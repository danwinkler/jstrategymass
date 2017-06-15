package com.danwink.strategymass.game.objects;

import com.danwink.dsync.sync.SyncObject;

public class Player extends SyncObject<Player>
{
	public int playerId;
	public String name;
	public int team;
	public int money = 10;
	public int unitsBuilt = 0;
	public int unitsKilled = 0;
	public int unitsLost = 0;
	
	public Player() {}
	
	public Player( int id )
	{
		this.playerId = id;
	}

	public void set( Player so )
	{
		this.playerId = so.playerId;
		this.money = so.money;
		this.team = so.team;
		this.name = so.name;
		this.unitsBuilt = so.unitsBuilt;
		this.unitsKilled = so.unitsKilled;
		this.unitsLost = so.unitsLost;
	}

	public void reset()
	{
		money = 10;
		unitsBuilt = 0;
		unitsKilled = 0;
		unitsLost = 0;
	}
}
