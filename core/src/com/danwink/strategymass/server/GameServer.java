package com.danwink.strategymass.server;

import java.io.IOException;
import java.util.ArrayList;

import com.danwink.strategymass.game.GameLogic;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.net.DServer;
import com.danwink.strategymass.net.DServer.Updateable;
import com.danwink.strategymass.net.SyncServer;
import com.danwink.strategymass.nethelpers.ClassRegister;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.Packets;
import com.danwink.strategymass.nethelpers.ServerMessages;

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
		
		server.on( ClientMessages.JOIN, (id, o) -> {
			Player p = logic.addPlayer( id );
			server.sendTCP( id, ServerMessages.JOINSUCCESS, p );
		});
		
		server.on( ClientMessages.BUILDUNIT, (id, o) -> {
			logic.buildUnit( id );
		});
		
		server.on( ClientMessages.MOVEUNITS, (int id, Packets.MoveUnitPacket p) -> {
			logic.moveUnits( id, p.pos, p.units );
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
		
		logic.newGame();
	}

	public void update( float dt )
	{
		logic.update( dt );
		sync.update();
	}
}
