package com.danwink.strategymass.game.objects;

import java.util.ArrayList;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.game.GameState;

public class Point
{
	public static float takeSpeed = 8;
	public static int nextId = 0;
	
	public int id = nextId++;
	public int team = -1;
	public Vector2 pos;
	public boolean isBase;
	public float taken = 0;
	
	public Point() {};
	
	public Point( float x, float y, boolean isBase, int team )
	{
		this.pos = new Vector2( x, y );
		this.isBase = isBase;
		this.team = team;
		this.taken = isBase ? 100 : 0;
	}
	
	public Point( float x, float y )
	{
		this( x, y, false, -1 );
	}
	
	public void update( float dt, GameState state )
	{
		if( !isCapturable( state ) ) return;
		
		float oldTaken = taken;
		
		int total = 0;
		int[] counts = new int[4];
		
		
		for( UnitWrapper uw : state.units )
		{
			Unit u = uw.getUnit();
			if( isHere( u.pos, state ) )
			{
				counts[u.team]++;
				total++;
			}
		}
		
		int maxNum = 0;
		int maxTeam = -1;
		for( int i = 0; i < counts.length; i++ )
		{
			if( counts[i] > maxNum )
			{
				maxNum = counts[i];
				maxTeam = i;
			}
			else if( counts[i] == maxNum && team == i )
			{
				maxTeam = i;
			}
		}
		
		if( maxTeam >= 0 )
		{
			int change = maxNum - (total-maxNum);
			float dTaken = change*dt * takeSpeed;
			if( team == maxTeam )
			{
				taken = MathUtils.clamp( taken+dTaken, 0, 100 );
			}
			else 
			{
				taken = taken-dTaken;
				if( taken < 0 ) 
				{
					taken *= -1;
					team = maxTeam;
				}
			}
		}
		
		if( taken != oldTaken )
		{
			state.map.partial = true;
		}
	}
	
	public boolean isHere( Vector2 u, GameState state )
	{
		int px = (int)(pos.x / state.map.tileWidth);
		int py = (int)(pos.y / state.map.tileHeight);
		int ux = (int)(u.x / state.map.tileWidth);
		int uy = (int)(u.y / state.map.tileHeight);
		
		return Math.abs( px-ux ) <= 1 && Math.abs( py-uy ) <= 1;
	}
	
	public static GridPoint2[] adjacentList = new GridPoint2[] {
		new GridPoint2( 0, -1 ),
		new GridPoint2( 1, 0 ),
		new GridPoint2( -1, 0 ),
		new GridPoint2( 0, 1 ),
	};
	
	public GridPoint2 findAjacent( Map m )
	{
		int px = (int)(pos.x / m.tileWidth);
		int py = (int)(pos.y / m.tileHeight);
		
		for( GridPoint2 l : adjacentList )
		{
			if( m.isPassable( px + l.x, py + l.y ) ) 
			{
				return new GridPoint2( px + l.x, py + l.y );
			}
		}
		return null;
	}
	
	public GridPoint2 randomAdjacent( Map m )
	{
		ArrayList<GridPoint2> adjs = adjacents( m );
		return adjs.get( MathUtils.random( adjs.size()-1 ) );
	}
	
	public ArrayList<GridPoint2> adjacents( Map m )
	{
		int px = (int)(pos.x / m.tileWidth);
		int py = (int)(pos.y / m.tileHeight);
		ArrayList<GridPoint2> adjs = new ArrayList<>();

		for( GridPoint2 l : adjacentList )
		{
			if( m.isPassable( px + l.x, py + l.y ) ) 
			{
				adjs.add( new GridPoint2( px + l.x, py + l.y ) );
			}
		}
		return adjs;
	}

	public boolean isCapturable( GameState state )
	{
		if( !isBase ) return true;
		
		for( Point p : state.map.points )
		{
			if( p == this ) continue;
			if( p.team == team ) return false;
		}
		
		return true;
	}
}
