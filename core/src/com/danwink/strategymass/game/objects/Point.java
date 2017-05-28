package com.danwink.strategymass.game.objects;

import com.badlogic.gdx.math.Vector2;

public class Point
{
	public int team = -1;
	public Vector2 pos;
	public boolean isBase;
	
	public Point() {};
	
	public Point( float x, float y, boolean isBase, int team )
	{
		this.pos = new Vector2( x, y );
		this.isBase = isBase;
		this.team = team;
	}
	
	public Point( float x, float y )
	{
		this( x, y, false, -1 );
	}
}
