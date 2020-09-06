package com.danwink.strategymass.screens.play;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.strategymass.MainMenu;
import com.danwink.strategymass.MenuScreen;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.ai.BotNamer;
import com.danwink.strategymass.game.MapFileHelper;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.server.GameServer;
import com.danwink.strategymass.server.LobbyPlayer;
import com.danwink.strategymass.server.ServerState;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class TrainScreen extends MenuScreen {
    public void build() {
        VisTextButton start = new VisTextButton("Start");
        VisTextButton back = new VisTextButton("Back");

        table.add(start).width(300).height(40);
        table.row();
        table.add(back).width(300).height(40);

        start.addListener(new ClickListener() {
            public void clicked(InputEvent e, float x, float y) {
                TrainSession session = new TrainSession();
                session.start();
            }
        });

        back.addListener(new ClickListener() {
            public void clicked(InputEvent e, float x, float y) {
                StrategyMass.game.setScreen(new MainMenu());
            }
        });
    }

    @Override
    public void render(float dt) {
        super.render(dt);
    }

    public class TrainSession {
        public void start() {
            GameServer server = new GameServer();

            // TODO: multiple classes refer to this singleton, assuming there's only one
            // server running at a time. We might want to change this so we can train
            // multiple at the same time
            StrategyMass.game.server = server;
            server.start();
            String mapName = MapFileHelper.getMaps().get(0);
            server.play.state.mapName = mapName;
            Map map = MapFileHelper.loadMap(mapName);
            server.server.setState(ServerState.PLAY);
            int nBots = 4;
            LobbyPlayer[] lobbyPlayers = new LobbyPlayer[nBots];
            for (int i = 0; i < nBots; i++) {
                LobbyPlayer p = new LobbyPlayer();
                lobbyPlayers[i] = p;
                p.name = BotNamer.getName();
                p.id = 100 + MathUtils.random(10000000);
                p.bot = true;
                p.slot = i;
                p.team = i % map.teams;
            }
            server.play.setUpFromLobby(lobbyPlayers);
            while (server.server.state == ServerState.PLAY) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(server.play.state.units.size());
            }
        }
    }
}
