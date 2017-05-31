package com.danwink.strategymass;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.strategymass.game.MapFileHelper;
import com.danwink.strategymass.screens.play.Play;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MapSelectScreen extends MenuScreen
{
	public void build()
	{
		VisSelectBox<String> select = new VisSelectBox<String>();
		select.setItems( MapFileHelper.getMaps().toArray( new String[0] ) );
		
		VisTextButton start = new VisTextButton( "Start" );
		start.addListener( new ClickListener() {
			public void clicked( InputEvent e, float x, float y )
			{
				StrategyMass.game.server.state.mapName = select.getSelected();
				StrategyMass.game.server.start();
				StrategyMass.game.setScreen( new Play() );
			}
		});
		
		table.add( select );
		table.row();
		table.add( start );
	}
}
