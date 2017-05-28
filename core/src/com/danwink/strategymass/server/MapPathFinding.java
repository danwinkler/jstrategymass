package com.danwink.strategymass.server;

import java.util.ArrayList;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.danwink.strategymass.game.objects.Map;

public class MapPathFinding
{
	public static class MapNode
	{
		final int index;
		final int x;
		final int y;
		final Array<Connection<MapNode>> connections;
		
		public MapNode( final int index, final int x, final int y )
		{
			this.index = index;
			this.x = x;
			this.y = y;
			connections = new Array<>(4);
		}
	}
	
	public static class MapGraph implements IndexedGraph<MapNode>
	{
		Array<MapNode> nodes;
		MapNode[][] nodeGrid;
		
		IndexedAStarPathFinder<MapNode> pathfinder;
		
		Heuristic<MapNode> heuristic = new ManhattanDistance();
		
		public MapGraph( Map map )
		{
			nodeGrid = new MapNode[map.height][map.width];
			
			nodes = new Array<>( map.height * map.width );
			
			int index = 0;
			for( int y = 0; y < map.height; y++ )
			{
				for( int x = 0; x < map.width; x++ )
				{
					nodeGrid[y][x] = new MapNode( index++, x, y );
					nodes.add( nodeGrid[y][x] );
				}
			}
			
			for( int y = 0; y < map.height; y++ )
			{
				for( int x = 0; x < map.width; x++ )
				{
					if( !map.isPassable( x, y ) ) 
					{
						continue;
					}
					
					if( x-1 >= 0 && map.isPassable( x-1, y ) ) 
					{
						nodeGrid[y][x].connections.add( new DefaultConnection<MapNode>(
							nodeGrid[y][x],
							nodeGrid[y][x-1]
						));
					}
					
					if( x+1 < map.width && map.isPassable( x+1, y ) ) 
					{
						nodeGrid[y][x].connections.add( new DefaultConnection<MapNode>(
							nodeGrid[y][x],
							nodeGrid[y][x+1]
						));
					}
					
					if( y-1 >= 0 && map.isPassable( x, y-1 ) ) 
					{
						nodeGrid[y][x].connections.add( new DefaultConnection<MapNode>(
							nodeGrid[y][x],
							nodeGrid[y-1][x]
						));
					}
					
					if( y+1 < map.height && map.isPassable( x, y+1 ) ) 
					{
						nodeGrid[y][x].connections.add( new DefaultConnection<MapNode>(
							nodeGrid[y][x],
							nodeGrid[y+1][x]
						));
					}
				}
			}
			
			pathfinder = new IndexedAStarPathFinder<>( this );
		}
		
		public ArrayList<GridPoint2> search( int x, int y, int tx, int ty )
		{
			GraphPath<MapNode> path = new DefaultGraphPath<>();
			boolean success = pathfinder.searchNodePath( nodeGrid[y][x], nodeGrid[ty][tx], heuristic, path );
			if( !success ) return null;
			
			ArrayList<GridPoint2> retPath = new ArrayList<>();
			path.forEach( n -> { retPath.add( new GridPoint2( n.x, n.y ) ); } );
			
			return retPath;
		}
		
		public Array<Connection<MapNode>> getConnections( MapNode fromNode )
		{
			return fromNode.connections;
		}

		public int getIndex( MapNode node )
		{
			return node.index;
		}

		public int getNodeCount()
		{
			return nodes.size;
		}
	}
	
	public static class ManhattanDistance implements Heuristic<MapNode>
	{
		public float estimate( final MapNode node, final MapNode endNode ) 
		{
			return Math.abs(endNode.x - node.x) + Math.abs(endNode.y - node.y);
		}
	}
}
