package com.danwink.strategymass;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.strategymass.screens.play.Play;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

public class ConnectScreen extends MenuScreen
{
	public void build()
	{
		VisTextField addr = new VisTextField();
		VisTextButton connect = new VisTextButton( "Connect" );
		
		table.add( addr );
		table.row();
		table.add( connect ).expandX();
		
		connect.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( new Play( addr.getText().trim() ) );
			}
		});
	}
}
