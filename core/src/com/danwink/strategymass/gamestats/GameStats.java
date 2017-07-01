package com.danwink.strategymass.gamestats;

import java.util.ArrayList;

import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Team;

public class GameStats
{
	public ArrayList<TeamStats> teamStats;
	public ArrayList<PlayerStats> playerStats;
	
	public GameStats() {}
	
	public void newGame( GameState state )
	{
		teamStats = new ArrayList<>();
		for( Team t : state.teams )
		{
			teamStats.add( new TeamStats( t ) );
		}
		
		playerStats = new ArrayList<>();
		for( Player p : state.players )
		{
			playerStats.add( new PlayerStats( p ) );
		}
	}

	public void update( GameState state )
	{
		teamStats.forEach( ts -> ts.update( state ) );
		//Commented out because it makes the gamestats object to big to send over the network
		//playerStats.forEach( ps -> ps.update( state ) );
	}
}
