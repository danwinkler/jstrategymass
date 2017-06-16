package com.danwink.strategymass.screens.play;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.dsync.DClient;
import com.danwink.libgdx.form.FormClient;
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
	
	FormClient fc;
	
	VisSelectBox<String> mapSelect;
	
	public void register( DClient client )
	{
		this.client = client;
		
		fc = new FormClient( client, ServerState.LOBBY );
	}
	
	public void show()
	{
		super.show();
	}
	
	public void build()
	{
		for( int i = 0; i < LobbyState.LOBBY_SIZE; i++ )
		{
			buildSlot( i );
		}
		
		mapSelect = new VisSelectBox<>();
		fc.add( "map", mapSelect );
		
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
		fc.add( "addbot", addBot );
		
		VisTextButton startGame = new VisTextButton( "Start" );
		fc.add( "start", startGame );
		
		table.add( mapSelect ).padTop( 30 ).colspan( 3 ).fillX();
		table.row();
		
		table.add( disc ).padTop( 30 );
		table.add( addBot ).padTop( 30 );
		table.add( startGame ).padTop( 30 );
	}
	
	public void buildSlot( int i )
	{
		VisTextButton name = new VisTextButton("");
		VisTextButton team = new VisTextButton("");
		VisTextButton kick = new VisTextButton("");
		
		fc.add( "name" + i, name );
		fc.add( "team" + i, team );
		fc.add( "kick" + i, kick );
		
		table.add( name ).fillX();
		table.add( team ).fillX();
		table.add( kick ).fillX();
		table.row();
	}
}
