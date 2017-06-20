package com.danwink.strategymass.server;

import java.util.HashMap;

import com.badlogic.gdx.math.MathUtils;
import com.danwink.dsync.DServer;
import com.danwink.dsync.sync.SyncServer;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.ai.Bot;
import com.danwink.strategymass.ai.SectorAI;
import com.danwink.strategymass.game.GameLogic;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.Packets;
import com.danwink.strategymass.nethelpers.ServerMessages;

public class PlayState implements com.danwink.dsync.ServerState
{
	DServer server;
	SyncServer sync;
	
	GameState state;
	GameLogic logic;
	
	HashMap<Integer, LobbyPlayer> playerKeyMap;
	
	public void register( DServer server )
	{
		this.server = server;
		
		sync = new SyncServer( server, ServerState.PLAY );
		
		state = new GameState();
		logic = new GameLogic( state, sync );
		
		server.on( ServerState.PLAY, ClientMessages.JOIN, (int id, Integer key) -> {
			Player p = logic.addPlayer( id );
			LobbyPlayer lp = playerKeyMap.get( key );
			p.team = lp.team;
			p.name = lp.name;
			
			server.sendTCP( id, ServerMessages.JOINSUCCESS, p.syncId );
		});
		
		server.on( ServerState.PLAY, ClientMessages.BUILDUNIT, (int id, Integer num) -> {
			if( num == null ) num = 1;
			for( int i = 0; i < num; i++ )
			{
				logic.buildUnit( id );
			}
		});
		
		server.on( ServerState.PLAY, ClientMessages.MOVEUNITS, (int id, Packets.MoveUnitPacket p) -> {
			logic.moveUnits( id, p.pos, p.units );
		});
	}
	
	public void setUpFromLobby( LobbyPlayer[] players )
	{
		playerKeyMap = new HashMap<>();
		int key = 0;
		for( LobbyPlayer lp : players )
		{
			if( lp != null )
			{
				if( lp.bot )
				{
					Bot a = new SectorAI();
					a.team = lp.team;
					a.name = lp.name;
					a.key = key++;
					
					playerKeyMap.put( a.key, lp );
					
					//TODO: playstate should hold its own reference to the server
					a.connect( StrategyMass.game.server.server );
					StrategyMass.game.server.addBot( a );
				} 
				else 
				{
					Player p = logic.addPlayer( lp.id );
					p.team = lp.team;
					p.name = lp.name;
					server.sendTCP( lp.id, ServerMessages.JOINSUCCESS, p.syncId );
				}
			}
		}
	}
	
	public void show()
	{
		logic.newGame();
	}
	
	public void update( float dt )
	{
		logic.update( dt );
		
		sync.update();
		
		if( logic.isGameOver() )
		{
			server.broadcastTCP( ServerMessages.GAMEOVER, null );
			//TODO: make this async, and longer (for a game over message to appear)
			try
			{
				Thread.sleep( 1000 );
			}
			catch( InterruptedException e )
			{
				e.printStackTrace();
			}
			server.setState( ServerState.LOBBY );
			StrategyMass.game.server.bots.forEach( b -> {
				b.stop();
			});
		}
	}
	
	public void hide()
	{
		
	}
}
