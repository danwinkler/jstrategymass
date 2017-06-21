package com.danwink.strategymass.screens.editor;

import java.util.Arrays;
import java.util.stream.Collectors;

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
		int baseX = 1;
		int baseY = 1;
		
		//This is a hacky way to figure out how many bases we need to make
		GridPoint2[] bases = mirror.getPoints( baseX, baseY, m );
		
		if( bases.length > 2 )
		{
			baseX = MathUtils.random( 2, m.width / 3 );
			baseY = MathUtils.random( 2, m.height / 3 );
		}
		else
		{
			baseX = MathUtils.random( 2, m.width / 3 );
			baseY = MathUtils.random( 2, m.height - 3 );
		}
		
		bases = mirror.getPoints( baseX, baseY, m );
		
		for( int i = 0; i < bases.length; i++ )
		{
			(new Brushes.BaseBrush( i )).draw( bases[i].x, bases[i].y, m );
		}
		
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
				Vector2[] positions = Arrays.asList( points )
					.stream()
					.map( p -> new Vector2( (p.x+.5f) * m.tileWidth, (p.y+.5f) * m.tileHeight ) )
					.collect( Collectors.toList() )
					.toArray( new Vector2[0] );
				
				for( Point p : m.points )
				{
					if( Arrays.asList( positions ).stream().anyMatch( a -> a.dst( p.pos ) < minPointDist ) )
					{
						continue searchPoints;
					}
				}
				
				Arrays.asList( points ).forEach( p -> point.draw( p.x, p.y, m ) );
				
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
			for( int y = 1; y < m.height-1; y++ )
			{
				for( int x = 1; x < m.width-1; x++ )
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
