package com.danwink.strategymass;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.strategymass.ai.Bot;
import com.danwink.strategymass.ai.SectorAI;
import com.danwink.strategymass.game.MapFileHelper;
import com.danwink.strategymass.screens.play.Play;
import com.danwink.strategymass.server.GameServer;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MapSelectScreen extends MenuScreen
{
	public void build()
	{
		Preferences prefs = Gdx.app.getPreferences( "com.danwink.strategymass.mapselect" );
		ArrayList<BotField> bots = new ArrayList<BotField>();
		
		VisSelectBox<String> select = new VisSelectBox<String>();
		ArrayList<String> maps = MapFileHelper.getMaps();
		select.setItems( maps.toArray( new String[0] ) );
		
		String selectedMap = prefs.getString( "map", "" );
		if( maps.contains( selectedMap ) )
		{
			select.setSelected( selectedMap );
		}
		
		VisTextButton start = new VisTextButton( "Start" );
		start.addListener( new ClickListener() {
			public void clicked( InputEvent e, float x, float y )
			{
				StrategyMass.game.server = new GameServer();
				StrategyMass.game.server.state.mapName = select.getSelected();
				StrategyMass.game.server.start();
				
				for( int i = 0; i < bots.size(); i++ )
				{
					BotField b = bots.get( i );
					b.setBot();
					prefs.putBoolean( "botFieldEnabled-" + i, b.enabled.getSelected() == Enabled.BOT );
					prefs.putInteger( "botFieldTeam-" + i, b.team.getSelected() );
				}
				
				prefs.putString( "map", select.getSelected() );
				prefs.flush();
				
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
			BotField b = new BotField( prefs.getBoolean( "botFieldEnabled-" + i, false ), prefs.getInteger( "botFieldTeam-" + i, 1 ) );
			
			bots.add( b );
			b.build();
		}
		
		table.add( select ).padTop( 20 ).colspan( 3 ).fillX().padBottom( 10 );
		table.row();
		table.add( cancel ).fillX();
		table.add( start ).fillX().colspan( 2 ).width( 100 );
	}
	
	public class BotField
	{
		boolean initEnabled = false;
		int initTeam = 1;
		
		VisSelectBox<Enabled> enabled;
		VisSelectBox<Integer> team;
		
		public BotField()
		{
			
		}
		
		public BotField( boolean enabled, int team )
		{
			initEnabled = enabled;
			initTeam = team;
		}
		
		public void build()
		{
			enabled = new VisSelectBox<>();
			enabled.setItems( Enabled.values() );
			enabled.setSelected( initEnabled ? Enabled.BOT : Enabled.OPEN );
			team = new VisSelectBox<>();
			team.setItems( 0, 1, 2, 3 );
			team.setSelected( initTeam );
			
			table.add( enabled ).fillX().colspan( 2 ).padBottom( 5 );
			table.add( team ).padBottom( 5 ).fillX().left();
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
