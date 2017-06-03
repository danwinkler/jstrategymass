package com.danwink.strategymass.screens.editor;

import com.danwink.strategymass.game.objects.Map;

public class Brushes
{
	public static class TileBrush extends Brush
	{
		int tile;
		
		public TileBrush( int tile )
		{
			this.tile = tile;
		}

		public void draw( int x, int y, Map m )
		{
			m.deletePoint( x, y );
			m.setTile( x, y, tile );
		}
	}
	
	public static class BaseBrush extends Brush
	{
		int team;
		
		public BaseBrush( int team )
		{
			this.team = team;
			this.mirrorable = false;
		}

		public void draw( int x, int y, Map m )
		{
			m.addPoint( x, y, true, team );
		}
	}
	
	public static class PointBrush extends Brush
	{
		public void draw( int x, int y, Map m )
		{
			m.addPoint( x, y );
		}
	}
}
