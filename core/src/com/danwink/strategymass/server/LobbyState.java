package com.danwink.strategymass.server;

import com.danwink.dsync.DServer;
import com.danwink.dsync.SyncServer;
import com.danwink.strategymass.game.MapFileHelper;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.ServerMessages;

public class LobbyState implements ServerStateInterface
{
	public static final int LOBBY_SIZE = 8;
	
	String map;
	LobbyPlayer[] slots = new LobbyPlayer[LOBBY_SIZE];
	
	public void register( DServer server )
	{
		map = MapFileHelper.getMaps().get( 0 );
		
		server.on( ServerState.LOBBY, ClientMessages.JOIN, (int id, String name) -> {
			LobbyPlayer p = new LobbyPlayer();
			p.id = id;
			p.slot = nextAvailableSlot( 0 );
			if( p.slot < 0 ) 
			{
				server.sendTCP( id, ServerMessages.JOINFAIL, "Lobby is full." );
				return;
			}
			p.name = name;
			slots[p.slot] = p;
			server.sendTCP( id, ServerMessages.JOINSUCCESS, null );
			server.broadcastTCP( ServerMessages.LOBBY_PLAYERS, slots );
		});
	}

	public void show()
	{
		
	}

	public void update( float dt )
	{
		
	}
	
	public int nextAvailableSlot( int start )
	{
		for( int i = 0; i < LOBBY_SIZE; i++ )
		{
			int index = (i+start) % LOBBY_SIZE;
			if( slots[index] == null )
			{
				return index;
			}
		}
		return -1;
	}
}
