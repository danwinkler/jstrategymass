package com.danwink.strategymass.game.objects;

import com.danwink.strategymass.net.SyncObject;

public class Team extends SyncObject<Team>
{
	public int id;
		
	public Team( int id )
	{
		this.id = id;
	}

	public void set( Team so )
	{
		this.id = so.id;
	}
}
