package com.danwink.strategymass;

import java.io.IOException;

import com.danwink.strategymass.net.DServer;
import com.danwink.strategymass.net.DServer.Updateable;
import com.danwink.strategymass.net.SyncServer;
import com.danwink.strategymass.nethelpers.ClassRegister;
import com.danwink.strategymass.nethelpers.ClientMessages;

public class GameServer implements Updateable
{
	public static final int TCP_PORT = 34124;
	public static final int UDP_PORT = 34125;
	
	DServer server;
	
	SyncServer sync;
	
	GameState state;
	GameLogic logic;
	
	public GameServer()
	{
		server = new DServer();
		server.register( ClassRegister.classes );
		server.register( SyncServer.registerClasses );
		
		sync = new SyncServer( server );
		
		state = new GameState();
		logic = new GameLogic( state, sync );
		
		server.listen( ClientMessages.JOIN, (id, o) -> {
			logic.addPlayer( id );
		});
	}
	
	public void start()
	{
		try
		{
			server.start( TCP_PORT, UDP_PORT );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
		server.startThread( this, 30 );
		
		logic.generateMap();
	}

	public void update( float dt )
	{
		
	}
}
