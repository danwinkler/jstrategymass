package com.danwink.strategymass.screens.play;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.dsync.DClient;
import com.danwink.libgdx.form.FormClient;
import com.danwink.strategymass.MenuScreen;
import com.danwink.strategymass.Screens;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.game.objects.Map;
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
	Minimap map;
	
	public void register( DClient client )
	{
		this.client = client;
		
		fc = new FormClient( client, ServerState.LOBBY );
		
		client.on( ServerState.LOBBY, ServerMessages.LOBBY_MAP, (Map m) -> {
			this.map.setMap( m );
		});
	}
	
	public void show()
	{
		super.show();
	}
	
	public void build()
	{
		Table slotTable = new Table();
		
		for( int i = 0; i < LobbyState.LOBBY_SIZE; i++ )
		{
			buildSlot( i, slotTable );
		}
		
		table.add( slotTable );
		
		mapSelect = new VisSelectBox<>();
		fc.add( "map", mapSelect );
		
		map = new Minimap();
		map.setSize( 300, 300 );	
		
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
		
		Table mapTable = new Table();
		
		mapTable.add( mapSelect ).padBottom( 10 ).fillX();
		mapTable.row();
		
		mapTable.add( map );
		mapTable.row();
		
		table.add( mapTable ).padLeft( 10 );
		
		table.row();
		
		Table buttonRow = new Table();
		buttonRow.add( disc ).width( 100 );
		buttonRow.add( addBot ).padLeft( 20 ).width( 100 );
		buttonRow.add( startGame ).padLeft( 20 ).width( 100 );
		
		table.add( buttonRow ).padTop( 30 ).colspan( 1 ).fillX();
		
		//table.setDebug( true, true );
	}
	
	public void buildSlot( int i, Table table )
	{
		VisTextButton name = new VisTextButton("");
		VisTextButton team = new VisTextButton("");
		VisTextButton kick = new VisTextButton("");
		
		fc.add( "name" + i, name );
		fc.add( "team" + i, team );
		fc.add( "kick" + i, kick );
		
		table.add( name ).fillX().width( 400 ).padBottom( 1 );
		table.add( team ).fillX().width( 30 ).padLeft( 1 ).padBottom( 1 );
		table.add( kick ).fillX().width( 50 ).padLeft( 1 ).padBottom( 1 );
		table.row();
	}
}
