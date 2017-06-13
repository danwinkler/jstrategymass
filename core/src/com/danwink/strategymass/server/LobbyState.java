package com.danwink.strategymass.server;

import com.badlogic.gdx.math.MathUtils;
import com.danwink.dsync.DServer;
import com.danwink.dsync.SyncServer;
import com.danwink.strategymass.ai.BotNamer;
import com.danwink.strategymass.game.MapFileHelper;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.ServerMessages;

public class LobbyState implements ServerStateInterface
{
	public static final int LOBBY_SIZE = 8;
	
	GameServer game;
	
	String map;
	LobbyPlayer[] slots = new LobbyPlayer[LOBBY_SIZE];

	public LobbyState( GameServer game )
	{
		this.game = game;
	}
	
	public void register( DServer server )
	{
		map = MapFileHelper.getMaps().get( 0 );
		
		server.on( ServerState.LOBBY, ClientMessages.JOIN, (int id, String name) -> {
			System.out.println( "server lobby " + name + " joined" );
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
		
		server.on( ServerState.LOBBY, ClientMessages.LOBBY_MOVEPLAYER, (int id, Integer playerId) -> {
			for( int i = 0; i < LOBBY_SIZE; i++ )
			{
				LobbyPlayer lp = slots[i];
				if( lp != null && lp.id == playerId )
				{
					int next = nextAvailableSlot( i );
					if( next >= 0 ) 
					{
						lp.slot = next;
						slots[next] = lp;
						slots[i] = null;
						server.broadcastTCP( ServerMessages.LOBBY_PLAYERS, slots );
						return;
					}
				}
			}
		});
		
		server.on( ServerState.LOBBY, ClientMessages.LOBBY_ADDBOT, (id, o) -> {
			LobbyPlayer p = new LobbyPlayer();
			p.name = BotNamer.getName();
			p.id = MathUtils.random( 10000000 );
			p.bot = true;
			p.slot = nextAvailableSlot( 0 );
			if( p.slot < 0 ) 
			{
				return;
			}
			slots[p.slot] = p;
			server.broadcastTCP( ServerMessages.LOBBY_PLAYERS, slots );
		});
		
		server.on( ServerState.LOBBY, ClientMessages.LOBBY_KICK, (int id, Integer playerId) -> {
			for( int i = 0; i < LOBBY_SIZE; i++ )
			{
				LobbyPlayer lp = slots[i];
				if( lp != null && lp.id == playerId )
				{
					slots[i] = null;
					server.broadcastTCP( ServerMessages.LOBBY_PLAYERS, slots );
					return;
				}
			}
		});
		
		server.on( ServerState.LOBBY, ClientMessages.LOBBY_STARTGAME, (id, o) -> {
			game.play.setUpFromLobby( slots );
			game.play.state.mapName = map;
			game.stateHandler = game.play;
			server.setState( ServerState.PLAY );
			game.play.show();
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
