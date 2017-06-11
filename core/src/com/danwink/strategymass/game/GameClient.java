package com.danwink.strategymass.game;

import java.io.IOException;

import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.ClientUnit;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.dsync.DClient;
import com.danwink.dsync.SyncClient;
import com.danwink.dsync.SyncServer;
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
	public int team = -1;
	public boolean gameOver = false;
	public String name = "";
	public boolean disconnected = false;
	
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
		
		//Direct messages
		client.on( DClient.CONNECTED, o -> {
			client.sendTCP( ClientMessages.JOIN, name );
			if( team >= 0 ) {
				client.sendTCP( ClientMessages.JOINTEAM, team );
			}
		});
		
		client.on( ServerMessages.JOINSUCCESS, (Integer id) -> {
			me = (Player)sync.get( id );
		});
		
		client.on( ServerMessages.GAMEOVER, o -> {
			gameOver = true;
			state.clearExceptPlayers();
		});
		
		client.on( DClient.DISCONNECTED, o -> {
			gameOver = true;
			disconnected = true;
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

	public void switchTeams()
	{
		client.sendTCP( ClientMessages.JOINTEAM, me.team == 0 ? 1 : 0 );
	}
}
