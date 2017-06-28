package com.danwink.strategymass.screens.play;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.danwink.dsync.DClient;
import com.danwink.strategymass.Assets;
import com.danwink.strategymass.AudioManager;
import com.danwink.strategymass.MainMenu;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.game.GameClient;
import com.danwink.strategymass.game.GameRenderer;
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.ClientUnit;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.Packets;
import com.danwink.strategymass.nethelpers.ServerMessages;
import com.danwink.strategymass.server.ServerState;

public class PlayScreen implements Screen, InputProcessor
{
	OrthographicCamera camera;
	
	GameClient client;
	
	DClient dclient;
	
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
	
	public void register( DClient dclient )
	{
		this.dclient = dclient;
		
		client = new GameClient();
		client.register( dclient );
		
		//This is a makeshift "onGameStart"
		client.client.on( ServerState.PLAY, ServerMessages.JOINSUCCESS, o -> {
			Point p = client.state.map.getBase( client.me.team );
			camera.position.x = p.pos.x;
			camera.position.y = p.pos.y;
			camera.update();
		});
	}
	
	public void show()
	{		
		camera = new OrthographicCamera();
		camera.setToOrtho( false );
		camera.zoom = 2;
		
		AudioManager.setCamera( camera );
		
		input = new InputMultiplexer();
		Gdx.input.setInputProcessor( input );
		ui = new PlayUI( input );
		input.addProcessor( this );
		
		client.start();
		renderer = new GameRenderer( client.state );
		
		shapeRenderer = new ShapeRenderer();
		
		//UI
		ui.create( this );
	}

	public void render( float dt )
	{
		client.update( dt );
		
		//Scrolling Logic
		boolean camChanged = false;
		if( Gdx.input.isKeyPressed( Input.Keys.LEFT ) || Gdx.input.isKeyPressed( Input.Keys.A ) )
		{
			camera.translate( -scrollSpeed * dt * camera.zoom, 0 );
			camChanged = true;
		}
		if( Gdx.input.isKeyPressed( Input.Keys.RIGHT ) || Gdx.input.isKeyPressed( Input.Keys.D ) )
		{
			camera.translate( scrollSpeed * dt * camera.zoom, 0 );
			camChanged = true;
		}
		if( Gdx.input.isKeyPressed( Input.Keys.DOWN ) || Gdx.input.isKeyPressed( Input.Keys.S ) )
		{
			camera.translate( 0, -scrollSpeed * dt * camera.zoom );
			camChanged = true;
		}
		if( Gdx.input.isKeyPressed( Input.Keys.UP ) || Gdx.input.isKeyPressed( Input.Keys.W ) )
		{
			camera.translate( 0, scrollSpeed * dt * camera.zoom );
			camChanged = true;
		}
		
		if( camChanged )
		{
			clampCamera();
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
			shapeRenderer.circle( uw.x, uw.y, 16 );
		}
	
		shapeRenderer.end();
		
		//Render UI
		if( client.me != null ) 
		{
			ui.setMoney( client.me.money );
		}
		ui.render();
		
		if( client.disconnected )
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
	
	public void clampCamera()
	{
		camera.position.x = MathUtils.clamp( camera.position.x, 0, client.state.map.width * client.state.map.tileWidth );
		camera.position.y = MathUtils.clamp( camera.position.y, 0, client.state.map.height * client.state.map.tileHeight );
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
		//ui.dispose();
	}

	public boolean keyDown( int keycode )
	{
		switch( keycode )
		{
		case Input.Keys.TAB:
			ui.showPlayers();
			return true;
		}
		

		return false;
	}

	public boolean keyUp( int keycode )
	{
		switch( keycode )
		{
		case Input.Keys.ESCAPE:
			ui.showExitDialog();
			return true;
		case Input.Keys.F2:
			renderer.toggleDebug();
			ui.toggleDebug();
			return true;
		case Input.Keys.Z:
			int buildCount = 1;
			if( Gdx.input.isKeyPressed( Input.Keys.CONTROL_LEFT ) ) 
			{
				buildCount = 10;
			}
			client.client.sendTCP( ClientMessages.BUILDUNIT, buildCount );
			return true;
		case Input.Keys.M:
			//Set view to whole map
			if( client.state.map == null ) return false;
			Map m = client.state.map;
			int mx = m.width * m.tileWidth;
			int my = m.height * m.tileHeight;
			float rx = mx / (float)Gdx.graphics.getWidth();
			float ry = my / (float)Gdx.graphics.getHeight();
			camera.zoom = rx < ry ? ry : rx;
			camera.position.set( mx * .5f, my * .5f, 0 );
			camera.update();
			return true;
		case Input.Keys.B:
			//Set view to base
			if( client.state.map == null || client.me == null ) return false;
			Point p = client.state.map.getBase( client.me.team );
			camera.position.set( p.pos.x, p.pos.y, 0 );
			camera.zoom = 1;
			camera.update();
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
		if( client.state.map == null ) return false;
		Vector3 projected = camera.unproject( new Vector3( screenX, screenY, 0 ) );
		
		if( projected.x < 0 || projected.y < 0 ) return false;
		
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
				Point p = client.state.map.getPoint( MathUtils.floor(projected.x / client.state.map.tileWidth), MathUtils.floor(projected.y / client.state.map.tileWidth) );
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
		if( client.state.map == null ) return false;
		
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
		if( client.state.map == null ) return false;
		
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
		if( client.state.map == null ) return false;
		
		Vector3 mousePosScreen = new Vector3( Gdx.input.getX(), Gdx.input.getY(), 0 );
		Vector3 worldA = camera.unproject( mousePosScreen.cpy() );
		
		camera.zoom += camera.zoom * amount * zoomSpeed;
		
		if( camera.zoom < .1f ) camera.zoom = .1f;
		if( camera.zoom > 100f ) camera.zoom = 100f;
		
		camera.update();
		
		Vector3 worldB = camera.unproject( mousePosScreen );
		
		if( amount < 0 )
		{
			camera.translate( worldA.x - worldB.x, worldA.y - worldB.y );
			clampCamera();
			camera.update();
		}
		
		return true;
	}
}
