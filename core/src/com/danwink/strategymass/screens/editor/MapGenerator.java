package com.danwink.strategymass.screens.editor;

import java.util.Arrays;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Point;

public class MapGenerator
{
	public static void generate( Map m, Mirror mirror )
	{
		//Vars
		final float minPointTiles = 7;
		final float minPointDist = minPointTiles * m.tileWidth;
		
		
		Brush base0 = new Brushes.BaseBrush( 0 );
		Brush base1 = new Brushes.BaseBrush( 1 );
		Brush point = new Brushes.PointBrush();
		Brush grass = new Brushes.TileBrush( Map.TILE_GRASS );
		Brush tree = new Brushes.TileBrush( Map.TILE_TREE );
		
		if( mirror instanceof Mirrors.None ) 
		{
			mirror = new Mirrors.XY();
		}
		
		//Clear points
		m.points.clear();
		
		//Fill map with trees
		for( int y = 0; y < m.height; y++ )
		{
			for( int x = 0; x < m.width; x++ )
			{
				tree.draw( x, y, m );
			}
		}
		
		//Add walls
		/*
		for( int x = 0; x < m.width; x++ )
		{
			tree.draw( x, 0, m );
			tree.draw( x, m.height-1, m );
		}
		
		for( int y = 0; y < m.height; y++ )
		{
			tree.draw( 0, y, m );
			tree.draw( m.width-1, y, m );
		}
		*/
		
		//Add bases
		//For this we have to special case the mirror
		int baseX = MathUtils.random( 2, m.width / 3 );
		int baseY = MathUtils.random( 2, m.height - 3 );
		
		GridPoint2[] bases = mirror.getPoints( baseX, baseY, m );
		
		base0.draw( bases[0].x, bases[0].y, m );
		base1.draw( bases[1].x, bases[1].y, m );
		
		int baseBoxWidth = MathUtils.random( 1, 3 );
		int baseBoxHeight = MathUtils.random( 1, 3 );
		
		for( GridPoint2 p : bases )
		{
			boxAround( p.x, p.y, baseBoxWidth, baseBoxHeight, m );
		}
		
		//Add points
		//Each loop we get 100 chances to find a spot for points, 
		//if we don't find a spot within 100 chances stop altogether
		addPoints:
		while( true )
		{
			searchPoints:
			for( int i = 0; i < 100; i++ )
			{
				GridPoint2[] points = mirror.getPoints( MathUtils.random( 2, m.width-3 ), MathUtils.random( 2, m.height-3 ), m );
				Vector2 a = new Vector2( (points[0].x+.5f) * m.tileWidth, (points[0].y+.5f) * m.tileHeight );
				Vector2 b = new Vector2( (points[1].x+.5f) * m.tileWidth, (points[1].y+.5f) * m.tileHeight );
				
				for( Point p : m.points )
				{
					if( a.dst( p.pos ) < minPointDist || b.dst( p.pos ) < minPointDist )
					{
						continue searchPoints;
					}
				}
				
				point.draw( points[0].x, points[0].y, m );
				point.draw( points[1].x, points[1].y, m );
				
				int boxWidth = MathUtils.random( 0, 3 );
				int boxHeight = MathUtils.random( 0, 3 );
				for( GridPoint2 p : points )
				{
					boxAround( p.x, p.y, boxWidth, boxHeight, m );
				}
				
				continue addPoints;
			}
			break;
		}
		
		//Flood fill
		for( int i = 0; i < 2; i++ )
		{
			//Copy tile array
			int[][] tiles = Arrays.stream( m.tiles ).map( a -> a.clone() ).toArray( int[][]::new );
			for( int y = 1; y < m.height-2; y++ )
			{
				for( int x = 1; x < m.width-2; x++ )
				{
					if( m.getTile( x, y ) != Map.TILE_TREE ) continue;
					
					int up = m.getTile( x, y+1 );
					int down = m.getTile( x, y-1 );
					int left = m.getTile( x-1, y );
					int right = m.getTile( x+1, y );
					
					if( up != Map.TILE_TREE || down != Map.TILE_TREE || left != Map.TILE_TREE || right != Map.TILE_TREE )
					{
						tiles[y][x] = Map.TILE_GRASS;
					}
				}
			}
			m.tiles = tiles;
		}
	}
	
	private static void boxAround( int x, int y, int w, int h, Map m )
	{
		int minX = x - w;
		int minY = y - h;
		int maxX = x + w;
		int maxY = y + h;
		
		minX = MathUtils.clamp( minX, 1, m.width-2 );
		maxX = MathUtils.clamp( maxX, 1, m.width-2 );
		minY = MathUtils.clamp( minY, 1, m.height-2 );
		maxY = MathUtils.clamp( maxY, 1, m.height-2 );
		
		for( int yy = minY; yy <= maxY; yy++ )
		{
			for( int xx = minX; xx <= maxX; xx++ )
			{
				if( m.getTile( xx, yy ) == Map.TILE_TREE )
				{
					m.setTile( xx, yy, Map.TILE_GRASS );
				}
			}
		}
	}
}
