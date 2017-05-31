package com.danwink.strategymass.screens.editor;

import com.danwink.strategymass.game.objects.Map;

public abstract class Mirror
{
	public abstract void draw( int x, int y, Brush b, Map m );
	
	public String toString()
	{
		return this.getClass().getSimpleName();
	}
}
