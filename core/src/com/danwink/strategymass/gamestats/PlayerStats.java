package com.danwink.strategymass.gamestats;

import java.util.ArrayList;

import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;

public class PlayerStats
{
	Player p;
	ArrayList<Integer> units = new ArrayList<>();
	
	public PlayerStats() {}
	
	public PlayerStats( Player p )
	{
		this.p = p;
	}

	public void update( GameState state )
	{
		units.add( (int)state.units.stream().filter( u -> u.getUnit().owner == p.syncId ).count() );
	}
}
