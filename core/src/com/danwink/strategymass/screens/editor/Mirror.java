package com.danwink.strategymass.screens.editor;

import com.badlogic.gdx.math.GridPoint2;
import com.danwink.strategymass.game.objects.Map;

public abstract class Mirror
{
	public abstract GridPoint2[] getPoints( int x, int y, Map m );
	
	public void draw( int x, int y, Brush b, Map m )
	{
		GridPoint2[] points = getPoints( x, y, m );
		
		for( GridPoint2 p : points )
		{
			b.draw( p.x, p.y, m );
		}
	}
	
	public String toString()
	{
		return this.getClass().getSimpleName();
	}
}
