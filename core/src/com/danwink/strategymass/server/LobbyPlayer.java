package com.danwink.strategymass.server;

import com.danwink.dsync.sync.SyncObject;

public class LobbyPlayer extends SyncObject<LobbyPlayer>
{
	public int id;
	public int slot;
	public int team;
	public String name;
	public boolean bot;
	
	public void set( LobbyPlayer p )
	{
		this.id = p.id;
		this.slot = p.slot;
		this.name = p.name;
		this.bot = p.bot;
		this.team = p.team;
	}
}
