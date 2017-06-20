package com.danwink.strategymass.screens.editor;

import com.badlogic.gdx.math.GridPoint2;
import com.danwink.strategymass.game.objects.Map;

public class Mirrors
{
	public static class None extends Mirror
	{
		public GridPoint2[] getPoints( int x, int y, Map m )
		{
			return new GridPoint2[] {
				new GridPoint2( x, y )
			};
		}	
	}
	
	public static class X extends Mirror
	{
		public GridPoint2[] getPoints( int x, int y, Map m )
		{
			return new GridPoint2[] {
				new GridPoint2( x, y ),
				new GridPoint2( (m.width-1)-x, y )
			};
		}	
	}
	
	public static class Y extends Mirror
	{
		public GridPoint2[] getPoints( int x, int y, Map m )
		{
			return new GridPoint2[] {
				new GridPoint2( x, y ),
				new GridPoint2( x, (m.height-1)-y )
			};
		}	
	}
	
	public static class XY extends Mirror
	{
		public GridPoint2[] getPoints( int x, int y, Map m )
		{
			return new GridPoint2[] {
				new GridPoint2( x, y ),
				new GridPoint2( (m.width-1)-x, (m.height-1)-y )
			};
		}	
	}
	
	public static class FourWay extends Mirror
	{
		public GridPoint2[] getPoints( int x, int y, Map m )
		{
			return new GridPoint2[] {
				new GridPoint2( x, y ),
				new GridPoint2( (m.width-1)-x, y ),
				new GridPoint2( x, (m.height-1)-y ),
				new GridPoint2( (m.width-1)-x, (m.height-1)-y ),
			};
		}	
	}
}
