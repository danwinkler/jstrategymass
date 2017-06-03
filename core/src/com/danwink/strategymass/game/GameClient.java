package com.danwink.strategymass.game;

import java.io.IOException;

import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.ClientUnit;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.net.DClient;
import com.danwink.strategymass.net.SyncClient;
import com.danwink.strategymass.net.SyncServer;
import com.danwink.strategymass.nethelpers.ClassRegister;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.ServerMessages;
import com.danwink.strategymass.screens.play.ClientLogic;
import com.danwink.strategymass.server.GameServer;

public class GameClient
{
	public DClient client;
	public SyncClient sync;
	
	public GameState state;
	public ClientLogic logic;
	public Player me;
	
	String addr;
	public int team = 0;
	public boolean gameOver = false;
	
	public GameClient()
	{
		this( "localhost" );
	}
	
	public GameClient( String addr )
	{
		client = new DClient();
		this.addr = addr;
	}
	
	public GameClient( DClient client )
	{
		this.client = client;
	}
	
	public void start() 
	{
		state = new GameState();
		logic = new ClientLogic( state );
		
		client.register( ClassRegister.classes );
		client.register( SyncServer.registerClasses );
		
		//Direct messages
		client.on( DClient.CONNECTED, o -> {
			client.sendTCP( ClientMessages.JOIN, team );
		});
		
		client.on( ServerMessages.JOINSUCCESS, (Integer id) -> {
			me = (Player)sync.get( id );
		});
		
		client.on( ServerMessages.GAMEOVER, o -> {
			gameOver = true;
		});
		
		client.on( DClient.DISCONNECTED, o -> {
			gameOver = true;
		});
		
		//Sync handlers
		sync = new SyncClient( client );
		sync.onAddAndJoin( Map.class, map -> {
			state.map = map;
		});
		
		sync.onAddAndJoin( Player.class, p -> {
			state.addPlayer( p );
		});
		
		sync.onAddAndJoin( Unit.class, u -> {
			state.addUnit( new ClientUnit( u ) );
		});
		
		sync.onRemove( Unit.class, id -> {
			state.removeUnit( id );
		});
		
		sync.onAddAndJoin( Bullet.class, b -> {
			state.bullets.add( b );
		});
		
		try
		{
			client.connect( addr, GameServer.TCP_PORT, GameServer.UDP_PORT );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	public void update( float dt )
	{
		client.update();
		logic.update( dt );
	}
}
