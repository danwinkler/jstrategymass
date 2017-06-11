package com.danwink.strategymass.server;

import java.io.IOException;
import java.util.ArrayList;

import com.danwink.dsync.DServer;
import com.danwink.dsync.DServer.Updateable;
import com.danwink.dsync.SyncServer;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.ai.Bot;
import com.danwink.strategymass.game.GameLogic;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.MapFileHelper;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;
import com.danwink.strategymass.nethelpers.ClassRegister;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.Packets;
import com.danwink.strategymass.nethelpers.ServerMessages;

public class GameServer implements Updateable
{
	public static final int TCP_PORT = 34124;
	public static final int UDP_PORT = 34125;
	
	public DServer server;
	
	ArrayList<Bot> bots;
	
	boolean nextMap = false;
	
	PlayState play = new PlayState();
	LobbyState lobby = new LobbyState();
	ServerStateInterface stateHandler = lobby;
	
	public GameServer()
	{
		server = new DServer();
		server.register( ClassRegister.classes );
		server.register( SyncServer.registerClasses );
		
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
		
		bots = new ArrayList<Bot>();
	}
	
	public void addBot( Bot b )
	{
		bots.add( b );
	}

	public void update( float dt )
	{
		
	}

	public void stop()
	{
		bots.forEach( bot -> bot.stop() );
		server.stop();
	}
}
