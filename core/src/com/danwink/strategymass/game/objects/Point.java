package com.danwink.strategymass.game.objects;

import java.util.Arrays;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.game.GameState;

public class Point
{
	public static float takeSpeed = 8;
	
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
		float oldTaken = taken;
		
		int total = 0;
		int[] counts = new int[4];
		int px = (int)(pos.x / state.map.tileWidth);
		int py = (int)(pos.y / state.map.tileHeight);
		
		for( Unit u : state.units )
		{
			int ux = (int)(u.pos.x / state.map.tileWidth);
			int uy = (int)(u.pos.y / state.map.tileHeight);
			
			if( Math.abs( px-ux ) <= 1 && Math.abs( py-uy ) <= 1 )
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
}
