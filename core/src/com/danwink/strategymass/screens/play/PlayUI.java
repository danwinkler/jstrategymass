package com.danwink.strategymass.screens.play;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.danwink.strategymass.MainMenu;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.game.MapFileHelper;
import com.danwink.strategymass.game.objects.Player;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;

public class PlayUI
{
	Stage stage;
	Table table;
	
	Label fps;
	Label money;
	TextButton addUnit;
	
	VisDialog playerInfo;
	
	public PlayUI( InputMultiplexer input )
	{
		stage = new Stage( new ScreenViewport());
		input.addProcessor( stage );
	}
	
	public void create() 
	{
		table = new Table();
		table.setFillParent( true );
		stage.addActor( table );

		//table.setDebug( true ); // This is optional, but enables debug lines for tables.
		
		addUnit = new VisTextButton( "Add Unit" );
		fps = new VisLabel( "FPS: 0" );
		money = new VisLabel( "Money: 0" );
		
		table.add( money ).top().left(); 
		table.add( fps ).top().right().expand();
		
		table.row();
		
		table.add( addUnit ).width( 100 ).height( 60 ).left().bottom().expand();
		
		table.pad( 2 );
		
		playerInfo = new VisDialog( "Players" );
		playerInfo.addListener( new InputListener() {
			public boolean keyUp( InputEvent e, int keycode )
			{
				if( keycode == Input.Keys.TAB ) 
				{
					playerInfo.hide( Actions.hide() );
					return true;
				}
				return false;
			}
		});
	}

	public void resize( int width, int height ) 
	{
		stage.getViewport().update( width, height, true );
	}
	
	public void showExitDialog()
	{
		VisDialog d = new VisDialog( "Menu" ) {
			public void result( Object obj )
			{
				String command = (String)obj;
				switch( command )
				{
				case "exit":
					if( StrategyMass.game.server != null )
					{
						StrategyMass.game.server.stop();
					}
					StrategyMass.game.setScreen( new MainMenu() );
					return;
				case "return":
					return;
				}
			}
		};
		
		d.getTitleLabel().setAlignment( Align.center );
		
		Table t = d.getButtonsTable();
		
		d.setObject( t.add( new VisTextButton( "Return to Game" ) ).fillX().getActor(), "return" );
		t.row();
		d.setObject( t.add( new VisTextButton( "Exit to Main Menu" ) ).fillX().getActor(), "exit" );
		
		d.show( stage );
	}

	public void render()
	{
		fps.setText( "FPS: " + Gdx.graphics.getFramesPerSecond() );
		
		stage.act( Gdx.graphics.getDeltaTime() );
		stage.draw();
	}
	
	public void setMoney( int amt )
	{
		money.setText( "Money: " + amt );
	}

	public void dispose() 
	{
		stage.dispose();
	}
	
	public void showPlayers( ArrayList<Player> players )
	{
		Table t = playerInfo.getContentTable();
		t.clearChildren();
		
		t.add( "Name" );
		t.add( "Team" );
		t.add( "Units Built" );
		t.add( "Units Killed" );
		t.add( "Units Lost" );
		t.row();
		
		for( Player p : players )
		{
			t.add( p.name );
			t.add( "" + p.team );
			t.add( "" + p.unitsBuilt );
			t.add( "" + p.unitsKilled );
			t.add( "" + p.unitsLost );
			t.row();
		}
		
		playerInfo.show( stage, Actions.show() );
		playerInfo.setPosition(Math.round((stage.getWidth() - playerInfo.getWidth()) / 2), Math.round((stage.getHeight() - playerInfo.getHeight()) / 2));
	}
}
