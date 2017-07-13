package com.danwink.strategymass.server;

import java.io.IOException;
import java.util.ArrayList;

import com.danwink.dsync.DServer;
import com.danwink.dsync.DServer.Updateable;
import com.danwink.dsync.ServerStateManager;
import com.danwink.dsync.sync.SyncServer;
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

public class GameServer
{
	public static final int TCP_PORT = 34124;
	public static final int UDP_PORT = 34125;
	
	public DServer server;
	ServerStateManager stateManager;
	
	public ArrayList<Bot> bots;
	
	boolean nextMap = false;
	
	PlayState play = new PlayState();
	LobbyState lobby = new LobbyState( this );
	PostGameState postGame = new PostGameState();
	
	public GameServer()
	{
		server = new DServer();
		server.register( ClassRegister.classes );
		
		play.register( server );
		lobby.register( server );
		postGame.register( server );
		
		stateManager = new ServerStateManager( server );
		
		stateManager.add( ServerState.PLAY, play );
		stateManager.add( ServerState.LOBBY, lobby );
		stateManager.add( ServerState.POSTGAME, postGame );
		
		server.setState( ServerState.LOBBY );
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
		
		server.startThread( stateManager, 30 );
		
		bots = new ArrayList<Bot>();
	}
	
	public void addBot( Bot b )
	{
		bots.add( b );
	}

	public void stop()
	{
		bots.forEach( bot -> bot.stop() );
		server.stop();
	}
}
