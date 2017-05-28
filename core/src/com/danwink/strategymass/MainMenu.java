package com.danwink.strategymass;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.strategymass.screens.play.Play;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MainMenu extends MenuScreen
{
	public void build()
	{
		VisTextButton start = new VisTextButton( "Start" );
		VisTextButton editor = new VisTextButton( "Editor" );
		
		table.add( start );
		table.row();
		table.add( editor );
		
		start.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( new Play() );
			}
		});
		
		editor.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( new Editor() );
			}
		});
	}
}
