package com.danwink.strategymass.gameobjects;

import java.util.ArrayList;

import com.danwink.strategymass.net.SyncObject;

public class Map extends SyncObject
{
	public int tileWidth = 32;
	public int tileHeight = 32;
	public int width, height;
	public int[][] tiles;
	public ArrayList<Point> points;
	
	public Map() {}
	
	public Map( int w, int h )
	{
		this.width = w;
		this.height = h;
		tiles = new int[h][w];
		points = new ArrayList<>();
	}
}
