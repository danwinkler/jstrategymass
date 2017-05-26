package com.danwink.strategymass;

import java.util.ArrayList;
import java.util.HashMap;

import com.danwink.strategymass.gameobjects.Map;
import com.danwink.strategymass.gameobjects.Player;
import com.danwink.strategymass.gameobjects.Point;
import com.danwink.strategymass.gameobjects.Team;
import com.danwink.strategymass.gameobjects.Unit;

public class GameState
{
	ArrayList<Unit> units;
	HashMap<Integer, Unit> unitMap;
	Map map;
	ArrayList<Player> players;
	ArrayList<Team> teams;
	
	public GameState()
	{
		units = new ArrayList<>();
		unitMap = new HashMap<>();
		players = new ArrayList<>();
		teams = new ArrayList<>();
	}
}
