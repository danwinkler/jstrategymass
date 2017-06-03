package com.danwink.strategymass.game;

import java.util.ArrayList;
import java.util.HashMap;

import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.game.objects.Team;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;

public class GameState
{
	public ArrayList<UnitWrapper> units;
	public HashMap<Integer, UnitWrapper> unitMap;
	public Map map;
	public ArrayList<Player> players;
	public HashMap<Integer, Player> playerMap;
	public ArrayList<Team> teams;
	public ArrayList<Bullet> bullets;
	
	public String mapName = "test";
	
	public GameState()
	{
		units = new ArrayList<>();
		unitMap = new HashMap<>();
		players = new ArrayList<>();
		playerMap = new HashMap<>();
		teams = new ArrayList<>();
		bullets = new ArrayList<>();
	}
	
	public void clear()
	{
		units.clear();
		unitMap.clear();
		map = null;
		players.clear();
		playerMap.clear();
		teams.clear();
		bullets.clear();
	}
	
	public void addPlayer( Player p )
	{
		players.add( p );
		playerMap.put( p.playerId, p );
	}

	public void addUnit( UnitWrapper u )
	{
		units.add( u );
		unitMap.put( u.getUnit().syncId, u );
	}

	public void removeBullet( int id )
	{
		bullets.removeIf( b -> b.syncId == id );
	}

	public void removeUnit( int id )
	{
		units.remove( unitMap.remove( id ) );
	}
	
	public void removeUnitAtIndex( int index )
	{
		unitMap.remove( units.remove( index ).getUnit().syncId );
	}
}
