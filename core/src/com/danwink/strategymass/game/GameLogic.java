package com.danwink.strategymass.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.GridBucket;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.game.MapPathFinding.MapGraph;
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.MegaUnit;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.game.objects.RegularUnit;
import com.danwink.strategymass.game.objects.ServerUnit;
import com.danwink.strategymass.game.objects.Team;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;
import com.danwink.dsync.ListenerManager;
import com.danwink.dsync.sync.SyncServer;

public class GameLogic
{
	public static final float tickInterval = 1; 
	
	GameState state;
	SyncServer sync;

	MapGraph graph;
	
	float timeUntilNextTick = tickInterval;
	
	ListenerManager<TickListener> tickListeners;
	GridBucket<UnitWrapper> gridBucket;

	int maxUnits;
	
	public GameLogic( GameState state, SyncServer server )
	{
		this.state = state;
		this.sync = server;
		
		tickListeners = new ListenerManager<>();
	}
	
	public void newGame()
	{
		state.clearExceptPlayers();
		for( Player p : state.players )
		{
			p.reset();
			p.update = true;
		}
		
		setMap( MapFileHelper.loadMap( state.mapName ) );
		makeTeams( state.map.teams );
		gridBucket = new GridBucket<>( state.map.width, state.map.height, 1 );

		Preferences prefs = StrategyMass.getSettings();
		maxUnits = prefs.getInteger("maxunits");
	}
	
	public void makeTeams( int n )
	{
		state.teams.clear();
		for( int i = 0; i < n; i++ )
		{
			state.teams.add( new Team( i ) );
		}
	}
	
	public Map generateMap()
	{
		Map map = new Map( 31, 31 );
		for( int y = 0; y < map.height; y++ )
		{
			map.tiles[y][0] = 1;
			map.tiles[y][map.width-1] = 1;
		}
		for( int x = 0; x < map.width; x++ )
		{
			map.tiles[0][x] = 1;
			map.tiles[map.height-1][x] = 1;
		}
		
		map.addPoint( 4, 4, true, 0 );
		map.addPoint( map.width-4, map.height-4, true, 1 );
		
		map.addPoint( map.width/2, map.height/2 );
		
		return map;
	}
	
	public void setMap( Map map )
	{
		state.map = map;
		sync.add( map );
		
		graph = new MapGraph( map );
	}
	
	public Player addPlayer( int id )
	{
		Player p = new Player( id );
		state.addPlayer( p );
		sync.add( p );
		return p;
	}
	

	public Player getPlayer( int id )
	{
		return state.playerMap.get( id );
	}

	public void buildUnit( int id )
	{
		long playerUnitCount = state.units.stream().filter(u -> u.getUnit().owner == id ).count();
		int playerUnitMax = maxUnits / state.players.size();
		if( playerUnitCount < playerUnitMax ) {
			Player p = state.playerMap.get( id );
			if( p.money < 10 ) return;
			
			Point base = getTeamBase( p.team );
			
			if( base == null ) return;
			
			p.money -= 10;
			p.unitsBuilt++;
			p.update = true;
			
			Unit u = new RegularUnit();
			u.owner = id;
			u.team = p.team;
			
			GridPoint2 adj = state.map.findOpenAdjecentTile( MathUtils.floor(base.pos.x / state.map.tileWidth), MathUtils.floor(base.pos.y / state.map.tileHeight) );
			
			u.pos = base.pos.cpy();
			u.pos.x += (adj.x) * 33;
			u.pos.y += (adj.y) * 33;
			u.pos.x += MathUtils.random( -.01f, .01f );
			u.pos.y += MathUtils.random( -.01f, .01f );
			sync.add( u );
			state.addUnit( new ServerUnit( u ) );
		}
	}
	
	public Point getTeamBase( int team )
	{
		List<Point> bases = state.map.points.stream().filter(p -> p.isBase && p.team == team ).collect(Collectors.toList());
		if( bases.size() > 0 ) {
			return bases.get(MathUtils.random(bases.size()-1));
		}
		return null;
	}

	public void update( float dt )
	{
		gridBucket.clear();
		for( UnitWrapper uw : state.units ) {
			Unit u = uw.getUnit();
			int tx = MathUtils.floor(u.pos.x / state.map.tileWidth);
			int ty = MathUtils.floor(u.pos.y / state.map.tileHeight);
			gridBucket.put(uw, tx, ty, 0);
		}

		for( int i = 0; i < state.units.size(); i++ ) 
		{
			ServerUnit u = (ServerUnit)state.units.get( i ); 
			u.update( dt, this, state, gridBucket );
			if( u.getUnit().remove )
			{
				Player p = state.playerMap.get( u.getUnit().owner );
				p.unitsLost++;
				p.update = true;
				state.removeUnitAtIndex( i );
				i--;
			}
		}
		Collections.shuffle( state.units );
		
		for( int i = 0; i < state.bullets.size(); i++ )
		{
			Bullet b = state.bullets.get( i ); 
			b.update( dt, state );
			if( b.remove ) 
			{
				state.removeBullet( b.syncId );
			}
		}
		
		state.map.update( dt, state );
		
		timeUntilNextTick -= dt;
		if( timeUntilNextTick <= 0 ) 
		{
			timeUntilNextTick += tickInterval;
			tick();
		}
	}
	
	public void tick()
	{
		int[] pointCount = new int[4];
		for( Point p : state.map.points )
		{
			if( p.team >= 0 )
			{
				pointCount[p.team]++;
			}
		}
		
		for( Player p : state.players )
		{
			p.money += pointCount[p.team];
			p.update = true;
		}
		tickListeners.call( t->t.tick() );
	}

	public void moveUnits( int id, Vector2 pos, ArrayList<Integer> units )
	{
		int tx = MathUtils.floor(pos.x / state.map.tileWidth);
		int ty = MathUtils.floor(pos.y / state.map.tileHeight);
		
		if( tx < 0 || ty < 0 || tx >= state.map.width || ty >= state.map.height )
		{
			return;
		}
		
		for( Integer u : units )
		{
			UnitWrapper uw = state.unitMap.get( u );
			if( uw == null ) continue;
			Unit unit = uw.getUnit();
			if( unit.owner == id )
			{
				int x = MathUtils.floor(unit.pos.x / state.map.tileWidth);
				int y = MathUtils.floor(unit.pos.y / state.map.tileHeight);
				
				
				//graph.search returns null when it can't find a path
				ArrayList<GridPoint2> path = graph.search( x, y, tx, ty );
				if( path != null ) 
				{
					//TODO: move this into a move function on unit
					unit.setMove( pos, path );
				}
			}
		}
	}

	public void shootBullet( Unit unit, float heading )
	{
		Bullet b = new Bullet( unit.pos.cpy(), heading );
		b.team = unit.team;
		b.owner = unit.owner;
		sync.add( b );
		state.bullets.add( b );
	}
	
	public void shootBullet( Unit unit, float heading, int damage, boolean dieOnHit )
	{
		Bullet b = new Bullet( unit.pos.cpy(), heading );
		b.damage = damage;
		b.team = unit.team;
		b.owner = unit.owner;
		b.dieOnHit = dieOnHit;
		sync.add( b );
		state.bullets.add( b );
	}

	public boolean isGameOver()
	{
		int firstTeam = -1;
		for( Point p : state.map.points )
		{
			if( p.team != -1 )
			{
				if( firstTeam == -1 )
				{
					firstTeam = p.team;
				}
				else if( firstTeam != p.team )
				{
					return false;
				}
			}
		}
		return true;
	}

	public void onTick( TickListener tickListener )
	{
		tickListeners.on( tickListener );
	}
	
	public static interface TickListener
	{
		public void tick();
	}

	public void combineUnits( int owner, ArrayList<Integer> ids )
	{
		List<Unit> units = ids.stream()
			.map( id -> state.unitMap.get( id ) )
			.filter( uw -> uw != null )
			.map( uw -> uw.getUnit() )
			.filter( u -> u.owner == owner && u instanceof RegularUnit && u.targetAbsorb == -1 )
			.collect( Collectors.toList() );
		
		if( units.size() >= MegaUnit.NUM_UNITS_TO_CREATE )
		{
			Unit target = units.get( 0 );
			target.absorbCount = 0;
			target.targetAbsorb = target.syncId;
			
			int tx = MathUtils.floor(target.pos.x / state.map.tileWidth);
			int ty = MathUtils.floor(target.pos.y / state.map.tileHeight);
			for( int i = 1; i < MegaUnit.NUM_UNITS_TO_CREATE; i++ )
			{
				Unit u = units.get( i );
				int x = MathUtils.floor(u.pos.x / state.map.tileWidth);
				int y = MathUtils.floor(u.pos.y / state.map.tileHeight);
				
				
				//graph.search returns null when it can't find a path
				ArrayList<GridPoint2> path = graph.search( x, y, tx, ty );
				if( path != null )
				{
					u.setMove( target.pos, path );
					u.targetAbsorb = target.syncId;
				}
			}
		}
	}

	public void buildMegaUnit( int owner, Vector2 pos )
	{
		Player p = state.playerMap.get( owner );
		
		p.unitsBuilt++;
		p.update = true;
		
		Unit u = new MegaUnit();
		u.owner = owner;
		u.team = p.team;
		u.pos = pos.cpy();
		
		sync.add( u );
		state.addUnit( new ServerUnit( u ) );
	}
}
