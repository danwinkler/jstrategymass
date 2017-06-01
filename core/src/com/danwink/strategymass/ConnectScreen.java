package com.danwink.strategymass;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.strategymass.screens.play.Play;
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
		VisSelectBox<Integer> team = new VisSelectBox<>();
		team.setItems( new Integer[] { 0, 1, 2, 3 } );
		
		table.add( addr ).colspan( 2 ).padBottom( 10 );
		table.row();
		table.add( new VisLabel( "Team:" ) );
		table.add( team );
		table.row();
		table.add( cancel ).fillX();
		table.add( connect ).fillX();
		
		connect.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( new Play( addr.getText().trim(), team.getSelected() ) );
			}
		});
		
		cancel.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( new MainMenu() );
			}
		});
	}
}
