package com.danwink.strategymass.gamestats;

import java.util.ArrayList;

import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Team;

public class TeamStats
{
	public Team t;
	public ArrayList<Integer> units = new ArrayList<>();
	public ArrayList<Integer> points = new ArrayList<>();
	
	public TeamStats() {}
	
	public TeamStats( Team t )
	{
		this.t = t;
	}

	public void update( GameState state )
	{
		units.add( (int)state.units.stream().filter( u -> u.getUnit().team == t.id ).count() );
		points.add( (int)state.map.points.stream().filter( p -> p.team == t.id ).count() );
	}
}
