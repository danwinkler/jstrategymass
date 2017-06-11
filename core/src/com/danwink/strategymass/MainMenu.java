package com.danwink.strategymass;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.strategymass.screens.editor.Editor;
import com.danwink.strategymass.server.GameServer;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MainMenu extends MenuScreen
{
	public void build()
	{
		VisTextButton start = new VisTextButton( "Start" );
		VisTextButton connect = new VisTextButton( "Connect" );
		VisTextButton editor = new VisTextButton( "Editor" );
		VisTextButton settings = new VisTextButton( "Settings" );
		
		table.add( start ).width( 300 ).height( 40 );
		table.row();
		table.add( connect ).width( 300 ).height( 40 );
		table.row();
		table.add( editor ).width( 300 ).height( 40 );
		table.row();
		table.add( settings ).width( 300 ).height( 40 );
		
		
		start.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.server = new GameServer();
				StrategyMass.game.server.start();
				
				StrategyMass.game.setScreen( new MapSelectScreen() );
			}
		});
		
		connect.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( new ConnectScreen() );
			}
		});
		
		editor.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( new Editor() );
			}
		});
		
		settings.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( new SettingsScreen() );
			}
		});
	}
}
