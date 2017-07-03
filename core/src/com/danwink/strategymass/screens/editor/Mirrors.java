package com.danwink.strategymass.screens.editor;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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
	
	public static class ThreeRot extends Mirror
	{
		public GridPoint2[] getPoints( int x, int y, Map m )
		{
			float cX = m.width / 2.f;
			float cY = m.height / 2.f;
			
			Vector2 v = new Vector2( x-cX, y-cY );
			
			float len = v.len();
			float angle = v.angle() * MathUtils.degreesToRadians;
			
			float a1 = angle + (MathUtils.PI2/3.f);
			float a2 = angle + (MathUtils.PI2/3.f) * 2;
			
			return new GridPoint2[] {
				new GridPoint2( x, y ),
				new GridPoint2( MathUtils.round( cX + (MathUtils.cos( a1 ) * len) ), MathUtils.round( cY + (MathUtils.sin( a1 ) * len) ) ),
				new GridPoint2( MathUtils.round( cX + (MathUtils.cos( a2 ) * len) ), MathUtils.round( cY + (MathUtils.sin( a2 ) * len) ) ),
			};
		}	
	}
	
	public static class FourRot extends Mirror
	{
		public GridPoint2[] getPoints( int x, int y, Map m )
		{	
			return new GridPoint2[] {
				new GridPoint2( x, y ),
				new GridPoint2( (m.width-1) - y, x ),
				new GridPoint2( (m.width-1) - x, (m.height-1) - y ),
				new GridPoint2( y, (m.height-1) - x ),
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
