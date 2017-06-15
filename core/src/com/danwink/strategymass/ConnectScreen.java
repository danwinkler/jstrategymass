package com.danwink.strategymass;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.strategymass.screens.play.PlayScreen;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

public class ConnectScreen extends MenuScreen
{
	public void build()
	{
		VisTextField addr = new VisTextField();
		VisTextButton connect = new VisTextButton( "Connect" );
		VisTextButton cancel = new VisTextButton( "Back" );
		
		table.add( addr ).colspan( 2 ).padBottom( 10 );
		table.row();
		table.add( cancel ).fillX();
		table.add( connect ).fillX();
		
		connect.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				Screens.connected.setAddress( addr.getText().trim() );
				StrategyMass.game.setScreen( Screens.connected );
			}
		});
		
		cancel.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( Screens.mainMenu );
			}
		});
	}
}
