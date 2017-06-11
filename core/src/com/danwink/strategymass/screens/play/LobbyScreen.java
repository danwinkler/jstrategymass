package com.danwink.strategymass.screens.play;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.danwink.dsync.DClient;
import com.danwink.dsync.SyncClient;
import com.danwink.dsync.SyncServer;
import com.danwink.strategymass.MenuScreen;
import com.danwink.strategymass.nethelpers.ServerMessages;
import com.danwink.strategymass.server.LobbyPlayer;
import com.danwink.strategymass.server.LobbyState;
import com.danwink.strategymass.server.ServerState;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class LobbyScreen extends MenuScreen
{
	Slot[] slots = new Slot[LobbyState.LOBBY_SIZE];
	LobbyPlayer[] players;
	
	public void register( DClient client )
	{
		client.on( ServerState.LOBBY, ServerMessages.LOBBY_PLAYERS, (LobbyPlayer[] players) -> {
			players = players;
			updateSlots();
		});
	}
	
	public void show()
	{
		super.show();
	}
	
	public void build()
	{
		for( int i = 0; i < slots.length; i++ )
		{
			slots[i] = new Slot();
			slots[i].build();
		}
	}
	
	public void updateSlots()
	{
		for( int i = 0; i < players.length; i++ )
		{
			LobbyPlayer p = players[i];
			if( p != null )
			{
				slots[i].update( p );
			}
		}
	}
	
	public class Slot
	{
		VisLabel name;
		VisTextButton team;
		
		public void build()
		{
			name = new VisLabel();
			team = new VisTextButton("");
			
			table.add( name );
			table.add( team );
			table.row();
		}
		
		public void update( LobbyPlayer p )
		{
			name.setText( p.name );
			team.setText( p.team + "" );
		}
	}
}
