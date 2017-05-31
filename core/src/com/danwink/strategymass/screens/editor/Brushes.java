package com.danwink.strategymass.screens.editor;

import com.danwink.strategymass.game.objects.Map;

public class Brushes
{
	public static class TileBrush implements Brush
	{
		int tile;
		
		public TileBrush( int tile )
		{
			this.tile = tile;
		}

		public void draw( int x, int y, Map m )
		{
			m.tiles[y][x] = tile;
			m.deletePoint( x, y );
		}
	}
	
	public static class BaseBrush implements Brush
	{
		int team;
		
		public BaseBrush( int team )
		{
			this.team = team;
		}

		public void draw( int x, int y, Map m )
		{
			m.addPoint( x, y, true, team );
		}
	}
	
	public static class PointBrush implements Brush
	{
		public void draw( int x, int y, Map m )
		{
			m.addPoint( x, y );
		}
	}
}
