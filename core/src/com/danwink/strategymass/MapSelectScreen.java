package com.danwink.strategymass;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.strategymass.ai.Bot;
import com.danwink.strategymass.ai.SectorAI;
import com.danwink.strategymass.game.MapFileHelper;
import com.danwink.strategymass.screens.play.Play;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MapSelectScreen extends MenuScreen
{
	public void build()
	{
		ArrayList<BotField> bots = new ArrayList<BotField>();
		
		VisSelectBox<String> select = new VisSelectBox<String>();
		select.setItems( MapFileHelper.getMaps().toArray( new String[0] ) );
		
		VisTextButton start = new VisTextButton( "Start" );
		start.addListener( new ClickListener() {
			public void clicked( InputEvent e, float x, float y )
			{
				StrategyMass.game.server.state.mapName = select.getSelected();
				StrategyMass.game.server.start();
				
				for( BotField b : bots )
				{
					b.setBot();
				}
				
				StrategyMass.game.setScreen( new Play() );
			}
		});
		
		VisTextButton cancel = new VisTextButton( "Cancel" );
		cancel.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( new MainMenu() );
			}
		});
		
		for( int i = 0; i < 8; i++ )
		{
			BotField b = new BotField();
			
			if( i == 0 ) {
				b.initEnabled = true;
			}
			
			bots.add( b );
			b.build();
		}
		
		table.add( select ).padTop( 20 ).colspan( 2 ).fillX();
		table.row();
		table.add( start ).colspan( 2 ).fillX().padTop( 10 );
	}
	
	public class BotField
	{
		boolean initEnabled = false;
		int initTeam = 1;
		
		VisSelectBox<Enabled> enabled;
		VisSelectBox<Integer> team;
		
		public void build()
		{
			enabled = new VisSelectBox<>();
			enabled.setItems( Enabled.values() );
			enabled.setSelected( initEnabled ? Enabled.BOT : Enabled.OPEN );
			team = new VisSelectBox<>();
			team.setItems( 0, 1, 2, 3 );
			team.setSelected( initTeam );
			
			table.add( enabled );
			table.add( team );
			table.row();
		}
		
		public void setBot()
		{
			if( enabled.getSelected() == Enabled.BOT )
			{
				Bot a = new SectorAI();
				a.team = team.getSelected();
				a.connect( StrategyMass.game.server.server );
				StrategyMass.game.server.addBot( a );
			}
		}
	}
	
	public enum Enabled 
	{
		OPEN,
		BOT;
	}
}
