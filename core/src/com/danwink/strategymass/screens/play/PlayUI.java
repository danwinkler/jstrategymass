package com.danwink.strategymass.screens.play;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.danwink.strategymass.MainMenu;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.ai.Tiberius;
import com.danwink.strategymass.game.MapFileHelper;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;

public class PlayUI
{
	PlayScreen play;
	Stage stage;
	Table table;
	
	Label fps;
	Label money;
	TextButton addUnit;
	TextButton megaUnit;
	
	VisDialog playerInfo;
	
	VisWindow aiEditor;
	
	VisTable debugPanel;
	Label unitCount;
	
	float playerInfoTimer = 0;
	
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
		megaUnit = new VisTextButton( "Combine" );
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
		
		megaUnit.addListener( new ChangeListener() {
			public void changed( ChangeEvent event, Actor actor ) {
				play.client.client.sendTCP( ClientMessages.MEGAUNIT, play.selected );
			}
		});
		
		table.add( money ).top().left(); 
		table.add( fps ).top().right().expand();
		
		table.row();
		
		table.add( addUnit ).width( 100 ).height( 60 ).left().bottom();
		table.add( megaUnit ).width( 100 ).height( 60 ).left().bottom().expand().padLeft( 5 );
		
		megaUnit.setDisabled( true );
		
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
		
		aiEditor = new VisWindow( "AI Editor" );
		
		buildAIEditor();
		
		aiEditor.setResizable( true );
		aiEditor.setSize( 400, 600 );
		
		aiEditor.setVisible( false );
		stage.addActor( aiEditor );
		aiEditor.setPosition( 0, Gdx.graphics.getHeight() - aiEditor.getHeight() - 20 );
	}

	public void resize( int width, int height ) 
	{
		stage.getViewport().update( width, height, true );
	}
	
	public void toggleDebug()
	{
		debugPanel.setVisible( !debugPanel.isVisible() );
	}
	
	public void toggleAIEditor()
	{
		aiEditor.setVisible( !aiEditor.isVisible() );
	}
	
	public void setCombineButtonEnabled( boolean state )
	{
		megaUnit.setDisabled( !state );
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
		
		playerInfoTimer += Gdx.graphics.getDeltaTime();
		if( playerInfoTimer >= 1 )
		{
			buildPlayerInfo();
			playerInfoTimer -= 1;
		}
	}
	
	public void setMoney( int amt )
	{
		money.setText( "Money: " + amt );
	}

	public void dispose() 
	{
		//stage.dispose();
	}
	
	public void buildPlayerInfo()
	{
		Table t = playerInfo.getContentTable();
		t.clearChildren();
		
		t.add( "Name" );
		t.add( "Team" );
		t.add( "Units Built" );
		t.add( "Units Killed" );
		t.add( "Units Lost" );
		t.row();
		
		for( Player p : play.client.state.players )
		{
			t.add( p.name );
			t.add( "" + p.team );
			t.add( "" + p.unitsBuilt );
			t.add( "" + p.unitsKilled );
			t.add( "" + p.unitsLost );
			t.row();
		}
	}
	
	public void buildAIEditor()
	{
		List<Field> fields = Tiberius.fm.getFields();
		for( Field f : fields )
		{
			float value;
			try
			{
				value = f.getFloat( null );
				VisSlider s = new VisSlider( value * .01f, value * 10, value * .01f, false );
				s.setValue( value );
				VisLabel vLabel = new VisLabel( "" + value );
				
				
				addAIEditorSliderListener( s, f, vLabel );
				
				aiEditor.add( new VisLabel( f.getName() ) );
				aiEditor.add( s ).width( 200 );
				aiEditor.add( vLabel );
				aiEditor.row();
			}
			catch( IllegalArgumentException | IllegalAccessException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void addAIEditorSliderListener( VisSlider s, Field f, VisLabel vLabel )
	{
		s.addListener( new ChangeListener() {
			public void changed( ChangeEvent event, Actor actor )
			{
				try
				{
					f.setFloat( null, s.getValue() );
					vLabel.setText( "" + s.getValue() );
				}
				catch( IllegalArgumentException | IllegalAccessException e )
				{
					e.printStackTrace();
				}
			}					
		});
	}
	
	public void showPlayers()
	{
		buildPlayerInfo();
		
		playerInfo.show( stage, Actions.show() );
		playerInfo.setPosition(Math.round((stage.getWidth() - playerInfo.getWidth()) / 2), Math.round((stage.getHeight() - playerInfo.getHeight()) / 2));
	}
}
