package com.danwink.strategymass.game;

import java.util.ArrayList;
import java.util.Collections;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.game.MapPathFinding.MapGraph;
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Point;
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
		Player p = state.playerMap.get( id );
		if( p.money < 10 ) return;
		
		Point base = getTeamBase( p.team );
		
		if( base == null ) return;
		
		p.money -= 10;
		p.unitsBuilt++;
		p.update = true;
		
		Unit u = new Unit();
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
	
	public Point getTeamBase( int team )
	{
		for( Point p : state.map.points )
		{
			if( p.isBase && p.team == team ) {
				return p;
			}
		}
		return null;
	}

	public void update( float dt )
	{
		for( int i = 0; i < state.units.size(); i++ ) 
		{
			ServerUnit u = (ServerUnit)state.units.get( i ); 
			u.update( dt, this, state );
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
					unit.path = path;
					unit.onPath = 0;	
					unit.targetX = pos.x;
					unit.targetY = pos.y;
					unit.update = true;
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
}
