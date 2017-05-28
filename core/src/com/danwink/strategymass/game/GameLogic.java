package com.danwink.strategymass.game;

import java.util.ArrayList;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.game.objects.Team;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.net.SyncServer;
import com.danwink.strategymass.server.MapPathFinding;
import com.danwink.strategymass.server.MapPathFinding.MapGraph;

public class GameLogic
{
	GameState state;
	SyncServer sync;

	MapGraph graph;
	
	public GameLogic( GameState state, SyncServer server )
	{
		this.state = state;
		this.sync = server;
	}
	
	public void newGame()
	{
		state.clear();
		makeTeams( 2 );
		setMap( MapFileHelper.loadMap( "test" ) );
	}
	
	public void makeTeams( int n )
	{
		state.teams.clear();
		for( int i = 0; i < n; i++ )
		{
			state.teams.add( new Team( n ) );
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

	public void buildUnit( int id )
	{
		Unit u = new Unit();
		u.owner = id;
		u.team = state.playerMap.get( id ).team;
		u.pos = getTeamBase( u.team ).pos.cpy();
		u.pos.y -= 33;
		sync.add( u );
		state.addUnit( u );
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
			state.units.get( i ).update( dt, state );
		}
	}

	public void moveUnits( int id, Vector2 pos, ArrayList<Integer> units )
	{
		for( Integer u : units )
		{
			Unit unit = state.unitMap.get( u );
			if( unit.owner == id )
			{
				int x = (int)(unit.pos.x / state.map.tileWidth);
				int y = (int)(unit.pos.y / state.map.tileHeight);
				
				int tx = (int)(pos.x / state.map.tileWidth);
				int ty = (int)(pos.y / state.map.tileHeight);
				
				//graph.search returns null when it can't find a path
				ArrayList<GridPoint2> path = graph.search( x, y, tx, ty );
				if( path != null ) 
				{
					unit.path = path;
					unit.onPath = 0;	
				}
			}
		}
	}
}
