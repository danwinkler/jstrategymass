package com.danwink.strategymass.game;

import com.danwink.strategymass.AudioManager;
import com.danwink.strategymass.AudioManager.GameSound;
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.ClientUnit;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.MegaUnit;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.RegularUnit;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.dsync.DClient;
import com.danwink.dsync.sync.SyncClient;
import com.danwink.strategymass.nethelpers.ServerMessages;
import com.danwink.strategymass.screens.play.ClientLogic;
import com.danwink.strategymass.server.ServerState;

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
	public boolean isBot = false;
	
	public void register( DClient client )
	{
		this.client = client;
		
		//Direct messages
		client.on( ServerState.PLAY, ServerMessages.JOINSUCCESS, (Integer id) -> {
			me = (Player)sync.get( id );
		});
		
		client.on( ServerState.PLAY, ServerMessages.GAMEOVER, o -> {
			gameOver = true;
			state.clearExceptPlayers();
		});
		
		client.on( ServerState.PLAY, DClient.DISCONNECTED, o -> {
			gameOver = true;
			disconnected = true;
		});
		
		//Sync handlers
		sync = new SyncClient( client, ServerState.PLAY );
		sync.onAddAndJoin( Map.class, map -> {
			state.map = map;
		});
		
		sync.onAddAndJoin( Player.class, p -> {
			state.addPlayer( p );
		});
		
		sync.onAddAndJoin( RegularUnit.class, u -> {
			state.addUnit( new ClientUnit( u ) );
		});
		
		sync.onAddAndJoin( MegaUnit.class, u -> {
			state.addUnit( new ClientUnit( u ) );
		});
		
		sync.onRemove( RegularUnit.class, id -> {
			state.removeUnit( id );
		});
		
		sync.onRemove( MegaUnit.class, id -> {
			state.removeUnit( id );
		});
		
		sync.onAddAndJoin( Bullet.class, b -> {
			state.bullets.add( b );
		});
		
		if( !isBot )
		{
			sync.onAdd( Bullet.class, b -> {
				AudioManager.play( GameSound.THROW_SPEAR, b.pos );
			});
		}
	}
	
	public void start() 
	{
		state = new GameState();
		logic = new ClientLogic( state );
		logic.isBot = this.isBot;
	}

	public void update( float dt )
	{
		client.update();
		logic.update( dt );
	}
}
