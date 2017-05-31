package com.danwink.strategymass.screens.editor;

import com.danwink.strategymass.game.objects.Map;

public class Mirrors
{
	public static class None extends Mirror
	{
		public void draw( int x, int y, Brush b, Map m )
		{
			b.draw( x, y, m );
		}	
	}
	
	public static class X extends Mirror
	{
		public void draw( int x, int y, Brush b, Map m )
		{
			b.draw( x, y, m );
			b.draw( (m.width-1) - x, y, m );
		}	
	}
	
	public static class Y extends Mirror
	{
		public void draw( int x, int y, Brush b, Map m )
		{
			b.draw( x, y, m );
			b.draw( x, (m.height-1) - y, m );
		}	
	}
	
	public static class XY extends Mirror
	{
		public void draw( int x, int y, Brush b, Map m )
		{
			b.draw( x, y, m );
			b.draw( (m.width-1) - x, (m.height-1) - y, m );
		}
	}
}
