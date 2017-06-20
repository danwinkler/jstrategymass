package com.danwink.strategymass.screens.play;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.danwink.strategymass.MainMenu;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.game.MapFileHelper;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class PlayUI
{
	PlayScreen play;
	Stage stage;
	Table table;
	
	Label fps;
	Label money;
	TextButton addUnit;
	
	VisDialog playerInfo;
	
	VisTable debugPanel;
	Label unitCount;
	
	public PlayUI( InputMultiplexer input )
	{
		stage = new Stage( new ScreenViewport());
		input.addProcessor( stage );
	}
	
	public void create( PlayScreen play ) 
	{
		this.play = play;
		
		table = new Table();
		table.setFillParent( true );
		stage.addActor( table );

		//table.setDebug( true ); // This is optional, but enables debug lines for tables.
		
		addUnit = new VisTextButton( "Add Unit" );
		fps = new VisLabel( "FPS: 0" );
		money = new VisLabel( "Money: 0" );
		
		addUnit.addListener( new ClickListener() {
			public void clicked( InputEvent e, float x, float y ) {
				int buildCount = 1;
				if( Gdx.input.isKeyPressed( Input.Keys.CONTROL_LEFT ) || Gdx.input.isKeyPressed( Input.Keys.ALT_LEFT ) ) 
				{
					buildCount = 10;
				}
				play.client.client.sendTCP( ClientMessages.BUILDUNIT, buildCount );
			}
		});
		
		table.add( money ).top().left(); 
		table.add( fps ).top().right().expand();
		
		table.row();
		
		table.add( addUnit ).width( 100 ).height( 60 ).left().bottom().expand();
		
		//not sure minimap is important when you have full zoom control
		//table.add( new Minimap() ).right().bottom().expand();
		
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
		
		debugPanel = new VisTable();
		debugPanel.setVisible( false );
		debugPanel.setFillParent( true );
		debugPanel.top().left();
		unitCount = new VisLabel();
			
		debugPanel.add( unitCount ).padTop( 30 );
		stage.addActor( debugPanel );
	}

	public void resize( int width, int height ) 
	{
		stage.getViewport().update( width, height, true );
	}
	
	public void toggleDebug()
	{
		debugPanel.setVisible( !debugPanel.isVisible() );
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
		
		if( debugPanel.isVisible() )
		{
			unitCount.setText( "UNIT COUNT:" + play.client.state.units.size() );
		}
		
		stage.act( Gdx.graphics.getDeltaTime() );
		stage.draw();
	}
	
	public void setMoney( int amt )
	{
		money.setText( "Money: " + amt );
	}

	public void dispose() 
	{
		//stage.dispose();
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
	
	public class Minimap extends Actor
	{
		float width = 200, height = 200;
		
		public Minimap()
		{
			super();
			this.setSize( width, height );
		}
		
		public void draw( Batch batch, float parentActor )
		{
			SpriteBatch b = (SpriteBatch)batch;
			Map m = play.client.state.map;
			
			if( m == null ) return;
			
			float xScale = width / m.width;
			float yScale = height / m.height;
			
			for( int y = 0; y < m.height; y++ )
			{
				for( int x = 0; x < m.width; x++ )
				{
					int tId = m.tiles[y][x];
					batch.draw( 
						tId == Map.TILE_TREE ? play.renderer.tree : play.renderer.grass, 
						getX() + x*xScale, 
						getY() + y*yScale,
						xScale, 
						yScale
					);
				}
			}
		}
	}
}
