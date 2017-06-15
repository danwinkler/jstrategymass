package com.danwink.strategymass.server;

import com.badlogic.gdx.math.MathUtils;
import com.danwink.dsync.DServer;
import com.danwink.dsync.sync.SyncServer;
import com.danwink.strategymass.ai.BotNamer;
import com.danwink.strategymass.game.MapFileHelper;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.ServerMessages;

public class LobbyState implements com.danwink.dsync.ServerState
{
	public static final int LOBBY_SIZE = 8;
	
	GameServer game;
	
	String map;
	String[] maps;
	LobbyPlayer[] slots = new LobbyPlayer[LOBBY_SIZE];

	public LobbyState( GameServer game )
	{
		this.game = game;
	}
	
	public void register( DServer server )
	{
		maps = MapFileHelper.getMaps().toArray( new String[0] );
		map = maps[0];
		
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
			server.sendTCP( id, ServerMessages.LOBBY_MAPLIST, maps );
			server.sendTCP( id, ServerMessages.LOBBY_MAP, map );
			server.broadcastTCP( ServerMessages.LOBBY_PLAYERS, slots );
		});
		
		server.on( ServerState.LOBBY, ClientMessages.LOBBY_UPDATE, (id, o) -> {
			server.sendTCP( id, ServerMessages.LOBBY_MAPLIST, maps );
			server.sendTCP( id, ServerMessages.LOBBY_MAP, map );
			server.sendTCP( id, ServerMessages.LOBBY_PLAYERS, slots );
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
		
		server.on( ServerState.LOBBY, ClientMessages.LOBBY_CHANGETEAM, (int id, Integer playerId) -> {
			for( int i = 0; i < LOBBY_SIZE; i++ )
			{
				LobbyPlayer lp = slots[i];
				if( lp != null && lp.id == playerId )
				{
					lp.team = (lp.team+1) % 2;
					server.broadcastTCP( ServerMessages.LOBBY_PLAYERS, slots );
					return;
				}
			}
		});
		
		server.on( ServerState.LOBBY, ClientMessages.LOBBY_SETMAP, (int id, String map) -> {
			this.map = map;
			server.broadcastTCP( ServerMessages.LOBBY_MAP, map );
		});
		
		server.on( ServerState.LOBBY, ClientMessages.LOBBY_STARTGAME, (id, o) -> {
			game.play.state.mapName = map;
			server.setState( ServerState.PLAY );
			game.play.setUpFromLobby( slots );
		});
	}

	public void show()
	{
		
	}

	public void update( float dt )
	{
		
	}
	
	public void hide()
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
