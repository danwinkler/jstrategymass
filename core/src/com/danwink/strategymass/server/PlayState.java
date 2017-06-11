package com.danwink.strategymass.server;

import com.danwink.dsync.DServer;
import com.danwink.dsync.SyncServer;
import com.danwink.strategymass.game.GameLogic;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.Packets;
import com.danwink.strategymass.nethelpers.ServerMessages;

public class PlayState implements ServerStateInterface
{
	DServer server;
	SyncServer sync;
	
	GameState state;
	GameLogic logic;
	
	public void register( DServer server )
	{
		this.server = server;
		
		sync = new SyncServer( server, ServerState.PLAY );
		
		state = new GameState();
		logic = new GameLogic( state, sync );
		
		server.on( ServerState.PLAY, ClientMessages.JOIN, (int id, String name) -> {
			Player p = logic.addPlayer( id );
			p.team = 0;
			p.name = name;
			server.sendTCP( id, ServerMessages.JOINSUCCESS, p.syncId );
		});
		
		server.on( ServerState.PLAY, ClientMessages.BUILDUNIT, (id, o) -> {
			logic.buildUnit( id );
		});
		
		server.on( ServerState.PLAY, ClientMessages.MOVEUNITS, (int id, Packets.MoveUnitPacket p) -> {
			logic.moveUnits( id, p.pos, p.units );
		});
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
		}
	}
}
