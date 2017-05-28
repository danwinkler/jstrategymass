package com.danwink.strategymass.game.objects;

import java.util.ArrayList;

import com.danwink.strategymass.net.SyncObject;

public class Map extends SyncObject<Map>
{
	public static final int TILE_GRASS = 0;
	public static final int TILE_TREE = 1;
	public static final int TILE_BASE = 2;
	public static final int TILE_POINT = 3;
	
	public String name;
	public int teams;
	public int tileWidth = 64;
	public int tileHeight = 64;
	public int width, height;
	public int[][] tiles;
	public ArrayList<Point> points;
	
	public Map() {}
	
	public Map( int w, int h )
	{
		this.width = w;
		this.height = h;
		tiles = new int[h][w];
		points = new ArrayList<>();
	}
	
	public void addPoint( int x, int y, boolean isBase, int team )
	{
		Point p = new Point( x * tileWidth + (tileWidth/2), y * tileHeight + (tileHeight/2), isBase, team );
		tiles[x][y] = isBase ? TILE_BASE : TILE_POINT;
		points.add( p );
	}
	
	public void addPoint( int x, int y )
	{
		addPoint( x, y, false, -1 );
	}
	
	//This shouldn't ever happen because the map is only sent once
	public void set( Map so )
	{
		this.tileWidth = so.tileWidth;
		this.tileHeight = so.tileHeight;
		this.width = so.width;
		this.height = so.height;
		this.tiles = so.tiles;
		this.points = so.points;
	}

	public boolean isPassable( int x, int y )
	{
		if( x < 0 || y < 0 || x >= width || y >= height ) return false;
		return tiles[y][x] == 0;
	}
}
