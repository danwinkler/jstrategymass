package com.danwink.strategymass.screens.play;

import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.strategymass.MainMenu;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.game.GameClient;
import com.danwink.strategymass.game.GameRenderer;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.ClientUnit;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;
import com.danwink.strategymass.net.DClient;
import com.danwink.strategymass.net.SyncClient;
import com.danwink.strategymass.net.SyncServer;
import com.danwink.strategymass.nethelpers.ClassRegister;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.Packets;
import com.danwink.strategymass.nethelpers.ServerMessages;
import com.danwink.strategymass.server.GameServer;

public class Play implements Screen, InputProcessor
{
	OrthographicCamera camera;
	
	GameClient client;
	
	GameRenderer renderer;
	
	InputMultiplexer input;
	PlayUI ui;
	
	//Select box
	ShapeRenderer shapeRenderer;
	boolean selecting = false;
	Vector2 selectStart = new Vector2();
	Vector2 selectEnd = new Vector2();
	ArrayList<Integer> selected = new ArrayList<>();
	
	float scrollSpeed = 300;
	float zoomSpeed = .1f;
	
	String addr;
	int team;
	
	public Play()
	{
		this( "localhost", 0 );
	}
	
	public Play( String addr, int team )
	{
		this.addr = addr;
		this.team = team;
	}
	
	public void show()
	{		
		camera = new OrthographicCamera();
		camera.setToOrtho( false );
		camera.zoom = 2;
		
		input = new InputMultiplexer();
		Gdx.input.setInputProcessor( input );
		ui = new PlayUI( input );
		input.addProcessor( this );
		
		client = new GameClient( addr );
		client.team = team;
		client.start();
		renderer = new GameRenderer( client.state );
		
		shapeRenderer = new ShapeRenderer();
		
		//UI
		ui.create();
		
		ui.addUnit.addListener( new ClickListener() {
			public void clicked( InputEvent e, float x, float y ) {
				client.client.sendTCP( ClientMessages.BUILDUNIT );
			}
		});
	}

	public void render( float dt )
	{
		client.update( dt );
		
		//Scrolling Logic
		if( Gdx.input.isKeyPressed( Input.Keys.LEFT ) )
		{
			camera.translate( -scrollSpeed * dt * camera.zoom, 0 );
			camera.update();
		}
		if( Gdx.input.isKeyPressed( Input.Keys.RIGHT ) )
		{
			camera.translate( scrollSpeed * dt * camera.zoom, 0 );
			camera.update();
		}
		if( Gdx.input.isKeyPressed( Input.Keys.DOWN ) )
		{
			camera.translate( 0, -scrollSpeed * dt * camera.zoom );
			camera.update();
		}
		if( Gdx.input.isKeyPressed( Input.Keys.UP ) )
		{
			camera.translate( 0, scrollSpeed * dt * camera.zoom );
			camera.update();
		}
		
		//Clear screen
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
		
		//Render game
		renderer.render( camera );
		
		//Render select box and selected units
		shapeRenderer.setProjectionMatrix( camera.combined );
		shapeRenderer.begin( ShapeType.Line );
		shapeRenderer.setColor( 1, 0, 0, 1 );
		
		if( selecting )
		{
			shapeRenderer.rect( selectStart.x, selectStart.y, selectEnd.x - selectStart.x, selectEnd.y - selectStart.y );
		}
	
		for( Integer i : selected )
		{
			ClientUnit uw = (ClientUnit)client.state.unitMap.get( i );
			if( uw == null ) continue;
			shapeRenderer.rect( uw.x - 16, uw.y - 16, 32, 32 );
		}
		
		/*
		//Directly render unit positions of server
		for( int i = 0; i < StrategyMass.game.server.state.units.size(); i++ )
		{
			UnitWrapper uw = StrategyMass.game.server.state.units.get( i );
			Unit u = uw.getUnit();
			shapeRenderer.circle( u.pos.x, u.pos.y, 20 );
		}
		*/
		
		shapeRenderer.end();
		
		//Render UI
		if( client.me != null ) 
		{
			ui.setMoney( client.me.money );
		}
		ui.render();
		
		if( client.gameOver )
		{
			StrategyMass.game.setScreen( new MainMenu() );
		}
	}

	public void resize( int width, int height )
	{
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		
		camera.update();
		ui.resize( width, height );
	}

	public void pause()
	{
		
	}

	public void resume()
	{
		
	}

	public void hide()
	{
		dispose();
		renderer.dispose();
		shapeRenderer.dispose();
	}

	public void dispose()
	{
		ui.dispose();
	}

	public boolean keyDown( int keycode )
	{
		return false;
	}

	public boolean keyUp( int keycode )
	{
		if( keycode == Input.Keys.ESCAPE ) 
		{
			ui.showExitDialog();
			return true;
		}
		return false;
	}

	public boolean keyTyped( char character )
	{
		return false;
	}

	public boolean touchDown( int screenX, int screenY, int pointer, int button )
	{
		Vector3 projected = camera.unproject( new Vector3( screenX, screenY, 0 ) );
		
		if( button == Buttons.LEFT )
		{
			selecting = true;
			selectStart.set( projected.x, projected.y );
			selectEnd.set( projected.x, projected.y );
		} else if( button == Buttons.RIGHT )
		{
			int tile = client.state.map.getTileFromWorld( projected.x, projected.y );
			if( tile == Map.TILE_BASE || tile == Map.TILE_POINT )
			{
				Point p = client.state.map.getPoint( (int)(projected.x / client.state.map.tileWidth), (int)(projected.y / client.state.map.tileWidth) );
				GridPoint2 adj = p.findAjacent( client.state.map );
				projected.x = (adj.x + .5f) * client.state.map.tileWidth;
				projected.y = (adj.y + .5f) * client.state.map.tileHeight;
			}
			
			client.client.sendTCP( ClientMessages.MOVEUNITS, new Packets.MoveUnitPacket( new Vector2( projected.x, projected.y ), selected ) );
			
		}
		
		return true;
	}

	public boolean touchUp( int screenX, int screenY, int pointer, int button )
	{
		if( button == Buttons.LEFT ) {
			Vector3 projected = camera.unproject( new Vector3( screenX, screenY, 0 ) );
			selectEnd.set( projected.x, projected.y );
			
			selected = client.logic.getUnitIds( selectStart, selectEnd, client.me.playerId );
			
			selecting = false;
		}
		return true;
	}

	public boolean touchDragged( int screenX, int screenY, int pointer )
	{
		Vector3 projected = camera.unproject( new Vector3( screenX, screenY, 0 ) );
		selectEnd.set( projected.x, projected.y );
		return true;
	}

	public boolean mouseMoved( int screenX, int screenY )
	{
		return false;
	}

	public boolean scrolled( int amount )
	{
		Vector3 mousePosScreen = new Vector3( Gdx.input.getX(), Gdx.input.getY(), 0 );
		Vector3 worldA = camera.unproject( mousePosScreen.cpy() );
		
		camera.zoom += camera.zoom * amount * zoomSpeed;
		camera.update();
		
		Vector3 worldB = camera.unproject( mousePosScreen );
		
		if( amount < 0 )
		camera.translate( worldA.x - worldB.x, worldA.y - worldB.y );
		
		camera.update();
		
		return true;
	}
}
