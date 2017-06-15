package com.danwink.strategymass.screens.play;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.dsync.DClient;
import com.danwink.strategymass.MenuScreen;
import com.danwink.strategymass.Screens;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.ServerMessages;
import com.danwink.strategymass.server.LobbyPlayer;
import com.danwink.strategymass.server.LobbyState;
import com.danwink.strategymass.server.ServerState;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class LobbyScreen extends MenuScreen
{
	DClient client;
	
	Slot[] slots = new Slot[LobbyState.LOBBY_SIZE];
	LobbyPlayer[] players;
	
	VisSelectBox<String> mapSelect;
	
	public void register( DClient client )
	{
		this.client = client;
		
		client.on( ServerState.LOBBY, ServerMessages.LOBBY_PLAYERS, (LobbyPlayer[] players) -> {
			this.players = players;
			updateSlots();
		});
		
		client.on( ServerState.LOBBY, ServerMessages.LOBBY_MAP, (String map) -> {
			mapSelect.setDisabled( true );
			mapSelect.setSelected( map );
			mapSelect.setDisabled( false );
		});
		
		client.on( ServerState.LOBBY, ServerMessages.LOBBY_MAPLIST, (String[] maps) -> {
			mapSelect.setItems( maps );
		});
	}
	
	public void show()
	{
		super.show();
		
		client.sendTCP( ClientMessages.LOBBY_UPDATE );
	}
	
	public void build()
	{
		for( int i = 0; i < slots.length; i++ )
		{
			slots[i] = new Slot();
			slots[i].build();
		}
		
		mapSelect = new VisSelectBox<>();
		mapSelect.addListener( new ChangeListener() {
			public void changed( ChangeEvent event, Actor actor )
			{
				if( mapSelect.isDisabled() ) return;
				client.sendTCP( ClientMessages.LOBBY_SETMAP, mapSelect.getSelected() );
			}
		});
		
		VisTextButton disc = new VisTextButton( "Disconnect" );
		disc.addListener( new ChangeListener() {
			public void changed( ChangeEvent event, Actor actor )
			{
				client.stop();
				if( StrategyMass.game.server != null )
				{
					StrategyMass.game.server.stop();
					StrategyMass.game.server = null;
				}
				StrategyMass.game.setScreen( Screens.mainMenu );
			}			
		});
		
		VisTextButton addBot = new VisTextButton( "Add Bot" );
		addBot.addListener( new ChangeListener() {
			public void changed( ChangeEvent event, Actor actor )
			{
				client.sendTCP( ClientMessages.LOBBY_ADDBOT );
			}			
		});
		
		VisTextButton startGame = new VisTextButton( "Start" );
		startGame.addListener( new ChangeListener() {
			public void changed( ChangeEvent event, Actor actor )
			{
				client.sendTCP( ClientMessages.LOBBY_STARTGAME );
			}
		});
		
		table.add( mapSelect ).padTop( 30 ).colspan( 3 ).fillX();
		table.row();
		
		table.add( disc ).padTop( 30 );
		table.add( addBot ).padTop( 30 );
		table.add( startGame ).padTop( 30 );
	}
	
	public void updateSlots()
	{
		for( int i = 0; i < players.length; i++ )
		{
			LobbyPlayer p = players[i];
			slots[i].update( p );
		}
	}
	
	public class Slot
	{
		VisTextButton name;
		VisTextButton team;
		VisTextButton kick;
		LobbyPlayer p;
		
		public void build()
		{
			name = new VisTextButton("");
			team = new VisTextButton("");
			kick = new VisTextButton( "Kick" );
			
			name.addListener( new ChangeListener() {
				public void changed( ChangeEvent e, Actor a )
				{
					if( p == null ) return;
					client.sendTCP( ClientMessages.LOBBY_MOVEPLAYER, p.id );
				}
			});
			
			team.addListener( new ChangeListener() {
				public void changed( ChangeEvent e, Actor a )
				{
					if( p == null ) return;
					client.sendTCP( ClientMessages.LOBBY_CHANGETEAM, p.id );
				}
			});
			
			kick.setVisible( false );
			kick.addListener( new ChangeListener() {
				public void changed( ChangeEvent event, Actor actor )
				{
					client.sendTCP( ClientMessages.LOBBY_KICK, p.id );
				}
			});
			
			table.add( name ).fillX();
			table.add( team ).fillX();
			table.add( kick ).fillX();	
			table.row();
		}
		
		public void update( LobbyPlayer p )
		{
			this.p = p;
			if( p != null ) 
			{
				name.setText( p.name );
				team.setText( p.team + "" );
				kick.setVisible( p.bot );
			}
			else 
			{
				name.setText( "OPEN" );
				team.setText( "" );
				kick.setVisible( false );
			}
		}
	}
}
