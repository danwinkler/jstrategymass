package com.danwink.strategymass.game.objects;

import java.util.ArrayList;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.danwink.strategymass.game.GameState;
import com.danwink.dsync.PartialUpdatable;
import com.danwink.dsync.sync.SyncObject;

public class Map extends SyncObject<Map> implements PartialUpdatable<ArrayList<Point>>
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
		float px = x * tileWidth + (tileWidth/2);
		float py = y * tileHeight + (tileHeight/2);
		
		deleteBase( team );
		deletePoint( x, y );
		
		Point p = new Point( px, py, isBase, team );
		tiles[y][x] = isBase ? TILE_BASE : TILE_POINT;
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
		return tiles[y][x] == Map.TILE_GRASS;
	}
	
	public int getTileFromWorld( float x, float y )
	{
		int tx = (int)(x / tileWidth);
		int ty = (int)(y / tileHeight);
		
		return getTile( tx, ty );
	}
	
	public int getTile( int x, int y )
	{
		if( x < 0 || y < 0 || x >= width || y >= height ) return TILE_TREE;
		return tiles[y][x];
	}
	
	public void setTile( int x, int y, int tile )
	{
		if( x < 0 || y < 0 || x >= width || y >= height ) return;
		
		tiles[y][x] = tile;
	}
	
	public void deleteBase( int team )
	{
		for( int i = 0; i < points.size(); i++ )
		{
			Point p = points.get( i );
			if( p.isBase && p.team == team )
			{
				deletePoint( (int)(p.pos.x / tileWidth), (int)(p.pos.y / tileHeight) );
				i--;
			}
		}
	}
	
	public void deletePoint( int x, int y )
	{
		float px = x * tileWidth + (tileWidth/2);
		float py = y * tileHeight + (tileHeight/2);
		
		points.removeIf( p -> {
			if( p.pos.x == px && p.pos.y == py )
			{
				setTile( x, y, Map.TILE_GRASS );
				return true;
			}
			return false;
		});
	}
	
	public Point getPoint( int x, int y )
	{
		float px = x * tileWidth + (tileWidth/2);
		float py = y * tileHeight + (tileHeight/2);
		
		for( int i = 0; i < points.size(); i++ )
		{
			Point p = points.get( i );
			if( MathUtils.isEqual( px, p.pos.x ) && MathUtils.isEqual( py, p.pos.y ) )
			{
				return p;
			}
		}
		
		return null;
	}
	
	public Point getBase( int team )
	{
		for( Point p : points )
		{
			if( p.isBase && p.team == team )
			{
				return p;
			}
		}
		return null;
	}
	
	public void update( float dt, GameState state )
	{
		for( Point p : points )
		{
			p.update( dt, state );
		}
	}

	public void partialReadPacket( ArrayList<Point> e )
	{
		e.forEach( p -> points.forEach( tp -> {
			if( tp.id == p.id ) {
				tp.taken = p.taken;
				tp.team = p.team;
			}
		}));
	}

	public ArrayList<Point> partialMakePacket()
	{
		return points;
	}
	
	public static final GridPoint2[] adjacentCheckList = {
		new GridPoint2( 0, -1 ),
		new GridPoint2( -1, 0 ),
		new GridPoint2( 1, 0 ),
		new GridPoint2( 0, 1 )
	};
	
	public GridPoint2 findOpenAdjecentTile( int x, int y )
	{
		for( GridPoint2 gp : adjacentCheckList )
		{
			int tx = gp.x+x;
			int ty = gp.y+y;
			
			if( tx < 0 || ty < 0 || tx >= width || ty >= height ) continue;
			
			if( tiles[ty][tx] == TILE_GRASS )
			{
				return gp;
			}
		}
		return null;
	}
}
