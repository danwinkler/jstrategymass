package com.danwink.strategymass.ai;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.danwink.fieldaccess.FieldManager;
import com.danwink.strategymass.ai.LearnAI.BattleGroupNetwork.Input;
import com.danwink.strategymass.ai.LearnAI.BattleGroupNetwork.Output;
import com.danwink.strategymass.ai.MapAnalysis.Neighbor;
import com.danwink.strategymass.ai.MapAnalysis.Zone;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.game.objects.RegularUnit;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.Packets;

import basicneuralnetwork.NeuralNetwork;
import basicneuralnetwork.activationfunctions.ActivationFunction;

/**
 * Every tick, rank all owned zones by how much we want to reinforce it
 * 
 * When units are built, they are added to a temp group, and then move to the #1
 * reinforce zone When two armies are next to each other, they merge.
 * 
 * Global Reinforce score function - How many of our own units are there + Is a
 * border zone + zone neighbors zone with lots of enemies + The fewer neighbors
 * a zone has, the better - Distance from home base (normalized by max) + Zone
 * is empty + Zone is next to zone owned by strongest team
 * 
 * Group specific reinforce score function + Zones you are currently in get a
 * point bonus (to dissuade moving around) - Distance to point - zone is visible
 * to lots of enemies
 * 
 * Groups
 * 
 * @author dan
 *
 */
public class LearnAI extends Bot {
    public static FieldManager fm;

    static {
        fm = new FieldManager(LearnAI.class);
    }

    MapAnalysis ma;
    AIAPI api;

    LinkedList<BattleGroup> groups = new LinkedList<>();
    LinkedList<BattleGroup> toAdd = new LinkedList<>();
    HashMap<Integer, BattleGroup> unitGroupMap = new HashMap<>();

    int teamStrength;

    int numAllies;
    int playerTeamIndex;

    BattleGroupNetwork bgnet;

    public void reset() {

    }

    public void update(Player me, GameState state, float dt) {
        if (state.map == null || me == null)
            return;

        if (ma == null) {
            ma = new MapAnalysis();
            ma.build(state.map);

            api = new AIAPI(c);
            bgnet = new BattleGroupNetwork();

            // Calculate # of allies and what team we are on
            for (int i = 0; i < c.state.players.size(); i++) {
                Player p = c.state.players.get(i);
                if (p.syncId == c.me.syncId) {
                    playerTeamIndex = numAllies;
                }
                if (p.team == c.me.team) {
                    numAllies++;
                }
            }
        }

        // Build units if we have enough money
        while (me.money >= Unit.unitCost) {
            send(ClientMessages.BUILDUNIT, null);
            me.money -= Unit.unitCost;
        }

        // Add units to groups
        c.state.units.forEach(uw -> {
            Unit u = uw.getUnit();

            if (u.owner != c.me.playerId)
                return;

            if (!unitGroupMap.containsKey(u.syncId)) {
                BattleGroup myGroup = null;
                for (BattleGroup g : groups) {
                    if (g.location.dst(u.pos) < c.state.map.tileWidth * 5 && !g.isMoving()) {
                        myGroup = g;
                        break;
                    }
                }

                // If we still didnt find an army to add to, start a new one
                if (myGroup == null) {
                    myGroup = new BattleGroup(this);
                    myGroup.location = new Vector2(u.pos.x, u.pos.y);
                    groups.addFirst(myGroup);
                }

                myGroup.units.add(u);
                unitGroupMap.put(u.syncId, myGroup);
            }
        });

        Iterator<BattleGroup> groupIter = groups.iterator();
        while (groupIter.hasNext()) {
            BattleGroup g = groupIter.next();

            // Update
            g.update();

            // Combine groups
            if (!g.isMoving()) {
                Zone currentZone = ma.getZone(g.location.x, g.location.y);

                for (BattleGroup og : groups) {
                    if (g == og)
                        continue;
                    if (og.isMoving())
                        continue;
                    if (g.units.size() + og.units.size() > 100)
                        continue; // TODO: Move var to top

                    Zone ogZone = ma.getZone(og.location.x, og.location.y);

                    if (og.location.dst(g.location) < c.state.map.tileWidth * 5 && ogZone == currentZone) {
                        og.units.addAll(g.units);
                        for (Unit u : g.units) {
                            unitGroupMap.put(u.syncId, og);
                        }
                        g.units.clear();
                        break;
                    }
                }
            }

            // Remove group if it doesn't have any units
            if (g.units.size() == 0) {
                groupIter.remove();
            }
        }

        groups.addAll(toAdd);
        toAdd.clear();
    }

    public Zone nextExpandZone(Unit u) {
        Zone z = ma.getZone(u.pos.x, u.pos.y);
        // Get unclaimed neighbors and sort by distance
        List<Zone> untaken = z.neighbors.stream().filter(n -> n.z.p.team == -1) // Untaken points
                .sorted((a, b) -> a.distance - b.distance) // Sort by distance
                .map(n -> n.z) // Get zone
                .collect(Collectors.toList());

        // If we don't have any untaken neighbors, find the closest untaken point to
        // base
        if (untaken.size() == 0) {
            untaken = ma.zones.stream().filter(zone -> zone.p.team == -1)
                    .sorted((a, b) -> a.baseDistances[c.me.team] - b.baseDistances[c.me.team])
                    .collect(Collectors.toList());
        }

        // If there aren't any untaken points, return null
        if (untaken.size() == 0) {
            return null;
        }

        // Go to the n-th one, where n is our team index (so everyone goes to a
        // different one)
        return untaken.get(playerTeamIndex % untaken.size());
    }

    public class BattleGroup {
        LinkedList<Unit> units = new LinkedList<>();
        Vector2 location = new Vector2();
        int attackCooldown = 0;
        LearnAI t;

        public BattleGroup(LearnAI t) {
            this.t = t;
        }

        public void update() {
            // Remove dead units
            for (int i = 0; i < units.size(); i++) {
                if (units.get(i).remove) {
                    unitGroupMap.remove(units.remove(i).syncId);
                    i--;
                }
            }

            // If for some reason we end up with an invalid position, head home.
            Zone currentZone = ma.getZone(location.x, location.y);

            if (currentZone == null) {
                move(ma.zones.stream().filter(z -> z.p.isBase && z.p.team == c.me.team).findFirst().get());
                return;
            }

            if (!isMoving()) {
                Optional<NeighborScore> neighborScore = currentZone.neighbors.stream().map(n -> {
                    BattleGroupNetwork.Input input = BattleGroupNetwork.Input.capture(t.api, t.ma, currentZone, n.z,
                            n.distance, t.team, units.size());

                    if (input == null)
                        return new NeighborScore(null, 0);

                    double score = bgnet.call(input);

                    System.out.println(score);

                    return new NeighborScore(n, score);
                }).max(Comparator.comparing(ns -> ns.score));

                neighborScore.ifPresent(ns -> {
                    if (ns.score > bgnet.threshold) {
                        move(ns.n.z);
                    }
                });
            }
        }

        public void move(Zone z) {
            attackCooldown = 10;
            GridPoint2 locGrid = z.p.randomAdjacent(c.state.map);
            location.x = (locGrid.x + .5f) * c.state.map.tileWidth;
            location.y = (locGrid.y + .5f) * c.state.map.tileHeight;

            ArrayList<Integer> unitIdsToMove = new ArrayList<Integer>();
            for (Unit u : units) {
                unitIdsToMove.add(u.syncId);
            }
            send(ClientMessages.MOVEUNITS, new Packets.MoveUnitPacket(location, unitIdsToMove));
        }

        public boolean isMoving() {
            for (Unit u : units) {
                // Don't wait for MegaUnits
                if (u.isMoving() && u instanceof RegularUnit) {
                    return true;
                }
            }
            return false;
        }
    }

    public class ZoneScore {
        float score;
        Zone z;

        public ZoneScore(Zone z) {
            this.z = z;
        }
    }

    public class TeamScore {
        int strength;
        int team;

        public TeamScore(int team) {
            this.team = team;
        }
    }

    public void render(ShapeRenderer shape, SpriteBatch batch) {
        if (groups.isEmpty())
            return;
        BattleGroup g = groups.getLast();

        shape.begin(ShapeType.Line);

        Zone currentZone = ma.getZone(g.location.x, g.location.y);

        shape.circle(currentZone.p.pos.x, currentZone.p.pos.y, 30);

        shape.circle(g.location.x, g.location.y, 20);

        for (Unit u : g.units) {
            shape.line(u.pos, g.location);
        }

        shape.end();
    }

    public static class BattleGroupNetwork {
        public static final int numHiddenLayers = 3;
        public static final int numHiddenNodes = 6;
        public double threshold = 0;
        NeuralNetwork network;

        public BattleGroupNetwork() {
            network = new NeuralNetwork(Input.inputFields.size(), numHiddenLayers, numHiddenNodes, 1);

            network.setActivationFunction(ActivationFunction.TANH);

            // Load from file
            // CallBag<Input, Output> callbag = new CallBag<>();
            // callbag.load(Input.class, Output.class);
            // Collections.shuffle(callbag.calls);

            // Generate fake (but somewhat plausible) data
            CallBag<Input, Output> callbag = generateFakeInitialTrainingData();

            List<Call<Input, Output>> positives = callbag.calls.stream().filter(c -> c.output.attackScore > 0)
                    .collect(Collectors.toList());

            positives.forEach(c -> {
                network.train(c.input.toArray(), c.output.toArray());
            });

            callbag.calls.stream().filter(c -> c.output.attackScore < 0).limit(positives.size()).forEach(c -> {
                network.train(c.input.toArray(), new double[] { 0 });
            });
        }

        public double call(Input input) {
            double[] inputArr = input.toArray();

            double[] output = network.guess(inputArr);

            return output[0];
        }

        public static class Input implements CallInput {
            static List<Field> inputFields;

            static {
                inputFields = Arrays.asList(Input.class.getDeclaredFields()).stream()
                        .filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList());
            }

            double numUnitsInGroup; // Number of units in our battle group (normalized by number of units on map)
            double numEnemiesTargetZone; // Number of enemies at target zone (normalized by number of units on map)
            double numFriendliesTargetZone; // Number of friendlies at target zone (normalized by number of units on
                                            // map)
            double distanceTargetZone; // Distance to target zone (normalized by distance from our base to furthest
                                       // base)
            double targetZoneIsOwned; // 1 if target zone is owned, zero otherwise (TODO should this be -1 otherwise?)
            double numEnemiesNeighboringZones; // Number of enemies in zones neigboring target zone (normalized by
                                               // number of units on map)

            public static Input capture(AIAPI api, MapAnalysis ma, Zone current, Zone target, int targetDistance,
                    int team, int battleGroupSize) {
                Input input = new Input();

                int totalUnits = api.c.state.units.size(); // Total units on map
                int numEnemiesTargetZone = api.numUnitsInZone(target, ma, u -> u.team != team); // Number of enemies in
                                                                                                // target zone
                int numFriendliesTargetZone = api.numUnitsInZone(target, ma, u -> u.team == team); // Number of
                                                                                                   // friendlies in
                                                                                                   // target zone
                Point base = api.c.state.map.getBase(team);
                if (base == null)
                    return null;

                Zone baseZone = ma.getZone(base.pos.x, base.pos.y);
                int furthestBaseDistance = Arrays.stream(baseZone.baseDistances).max().getAsInt();
                int numEnemiesNeighboringZones = target.neighbors.stream()
                        .mapToInt(n -> api.numUnitsInZone(n.z, ma, u -> u.team != team)).sum();

                input.numUnitsInGroup = battleGroupSize / (double) totalUnits;
                input.numEnemiesTargetZone = numEnemiesTargetZone / (double) totalUnits;
                input.numFriendliesTargetZone = numFriendliesTargetZone / (double) totalUnits;
                input.distanceTargetZone = targetDistance / (double) furthestBaseDistance;
                input.targetZoneIsOwned = target.p.team == team ? 1 : -1;
                input.numEnemiesNeighboringZones = numEnemiesNeighboringZones / (double) totalUnits;
                return input;
            }

            @Override
            public String toLine() {
                return Arrays.stream(this.toArray()).mapToObj(String::valueOf).collect(Collectors.joining(";"));
            }

            @Override
            public void fromLine(String line) {
                String[] parts = line.split(";");
                assert parts.length == inputFields.size();
                for (int i = 0; i < inputFields.size(); i++) {
                    Field field = inputFields.get(i);
                    try {
                        field.set(this, Double.parseDouble(parts[i]));
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void fromArray(double[] arr) {
                for (int i = 0; i < inputFields.size(); i++) {
                    Field field = inputFields.get(i);
                    try {
                        field.set(this, arr[i]);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            public double[] toArray() {
                return Input.inputFields.stream().mapToDouble(f -> {
                    try {
                        return (double) f.get(this);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }).toArray();
            }
        }

        public static class Output implements CallOutput {
            double attackScore;

            public Output() {

            }

            public Output(double attackScore) {
                this.attackScore = attackScore;
            }

            @Override
            public String toLine() {
                return Double.toString(attackScore);
            }

            @Override
            public void fromLine(String line) {
                attackScore = Double.parseDouble(line);
            }

            public double[] toArray() {
                return new double[] { attackScore };
            }
        }
    }

    static interface CallInput {
        public String toLine();

        public void fromLine(String line);
    }

    static interface CallOutput {
        public String toLine();

        public void fromLine(String line);
    }

    static class Call<I extends CallInput, O extends CallOutput> {
        I input;
        O output;

        public Call(I input, O output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public String toString() {
            return input.toLine() + "=>" + output.toLine();
        }
    }

    static class CallBag<I extends CallInput, O extends CallOutput> {
        public static String saveName = "callbag.txt";

        ArrayList<Call<I, O>> calls = new ArrayList<>();

        public void add(I input, O output) {
            calls.add(new Call<I, O>(input, output));
        }

        public void save() {
            String[] content = calls.stream().map(c -> c.input.toLine() + "=>" + c.output.toLine())
                    .toArray(String[]::new);
            synchronized (saveName) {
                try {
                    Path path = Paths.get(saveName);
                    Files.write(path, Arrays.asList(content), StandardCharsets.UTF_8,
                            Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void load(Class<I> inputCls, Class<O> outputCls) {
            Path path = Paths.get(saveName);
            try {
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                lines.forEach(line -> {
                    String[] parts = line.split("=>");
                    assert parts.length == 2;

                    try {
                        I input = inputCls.getConstructor().newInstance();
                        input.fromLine(parts[0]);

                        O output = outputCls.getConstructor().newInstance();
                        output.fromLine(parts[1]);

                        calls.add(new Call<I, O>(input, output));
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void clear() {
            calls.clear();
        }
    }

    class NeighborScore {
        Neighbor n;
        double score;

        public NeighborScore(Neighbor n, double score) {
            this.n = n;
            this.score = score;
        }
    }

    /**
     * Tries to create a seed training data set based of some simple rules
     * 
     * @return
     */
    public static CallBag<Input, Output> generateFakeInitialTrainingData() {
        int training_rows = 100000;

        CallBag<BattleGroupNetwork.Input, BattleGroupNetwork.Output> callbag = new CallBag<>();

        for( int i = 0; i < training_rows; i++ ) {
            BattleGroupNetwork.Input input = new BattleGroupNetwork.Input();
            
            // Randomize fields
            input.fromArray(BattleGroupNetwork.Input.inputFields.stream().mapToDouble(f -> Math.random()).toArray());
            input.numUnitsInGroup = Math.random() * .2f;
            input.numEnemiesTargetZone = Math.random() * .2f;
            input.numEnemiesNeighboringZones = Math.random() * .4f;
            input.targetZoneIsOwned = Math.random() > .5 ? 1 : 0;            

            double output = 0;

            if( input.targetZoneIsOwned == 0 ) {
                output = (input.numUnitsInGroup / (input.numEnemiesTargetZone*1.25)) - 1;
            } else {
                output = (input.numEnemiesNeighboringZones / input.numFriendliesTargetZone) - 1;
            }

            output = MathUtils.clamp(output, -1, 1);

            callbag.add(input, new BattleGroupNetwork.Output(output));
        }

        return callbag;
    }
}
