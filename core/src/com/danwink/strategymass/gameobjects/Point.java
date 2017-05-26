package com.danwink.strategymass.gameobjects;

import com.badlogic.gdx.math.Vector2;

public class Point
{
	int team;
	Vector2 pos;
	boolean isBase;
	
	public Point() {};
	
	public Point( float x, float y )
	{
		this.pos = new Vector2( x, y );
	}
}
