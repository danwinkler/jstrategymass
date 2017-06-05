package com.danwink.strategymass;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

public class SettingsScreen extends MenuScreen
{
	public void build()
	{
		Preferences prefs = StrategyMass.getSettings();
		
		VisTextField name = new VisTextField();
		name.setText( prefs.getString( "name", "Player" ) );
		
		VisTextButton back = new VisTextButton( "Back" );
		VisTextButton save = new VisTextButton( "Save" );
		
		back.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( new MainMenu() );
			}
		});
		
		save.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				prefs.putString( "name", name.getText().trim() );
				
				StrategyMass.game.setScreen( new MainMenu() );
			}
		});
		
		table.add( new VisLabel( "Name:" ) );
		table.add( name );
		table.row();
		table.add( back );
		table.add( save );
	}
}
