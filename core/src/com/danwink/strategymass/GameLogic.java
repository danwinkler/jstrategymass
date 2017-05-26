package com.danwink.strategymass;

import com.danwink.strategymass.gameobjects.Map;
import com.danwink.strategymass.gameobjects.Point;
import com.danwink.strategymass.net.SyncServer;

public class GameLogic
{
	GameState state;
	SyncServer server;

	public GameLogic( GameState state, SyncServer server )
	{
		this.state = state;
		this.server = server;
	}
	
	public void generateMap()
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
		
		map.points.add( new Point( map.tileWidth*3.5f, map.tileHeight*3.5f ) );
		map.points.add( new Point( map.tileWidth*(map.width-3.5f), map.tileHeight*(map.height-3.5f) ) );
		
		state.map = map;
		server.add( map );
	}
	
	public void addPlayer( int id )
	{
		
	}

}
