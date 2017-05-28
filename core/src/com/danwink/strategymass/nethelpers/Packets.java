package com.danwink.strategymass.nethelpers;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

public class Packets
{
	public static class MoveUnitPacket
	{
		public Vector2 pos;
		public ArrayList<Integer> units;
		
		public MoveUnitPacket() {}
		
		public MoveUnitPacket( Vector2 pos, ArrayList<Integer> units )
		{
			this.pos = pos;
			this.units = units;
		}
	}
}
