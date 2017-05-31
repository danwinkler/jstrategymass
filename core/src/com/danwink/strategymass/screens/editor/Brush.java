package com.danwink.strategymass.screens.editor;

import com.danwink.strategymass.game.objects.Map;

public abstract class Brush
{
	public boolean mirrorable = true;
	
	public abstract void draw( int x, int y, Map m );
}
