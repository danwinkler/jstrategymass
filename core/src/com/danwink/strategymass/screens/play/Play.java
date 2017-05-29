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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.game.GameRenderer;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Unit;
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
	
	DClient client;
	SyncClient sync;
	
	GameState state;
	ClientLogic logic;
	Player me;
	GameRenderer renderer;
	
	InputMultiplexer input;
	PlayUI ui;
	
	//Select box
	ShapeRenderer selectBoxRenderer;
	boolean selecting = false;
	Vector2 selectStart = new Vector2();
	Vector2 selectEnd = new Vector2();
	ArrayList<Integer> selected = new ArrayList<>();
	
	float scrollSpeed = 300;
	float zoomSpeed = .1f;

	public void show()
	{
		camera = new OrthographicCamera();
		camera.setToOrtho( false );
		camera.zoom = 2;
		
		input = new InputMultiplexer();
		Gdx.input.setInputProcessor( input );
		ui = new PlayUI( input );
		input.addProcessor( this );
		
		state = new GameState();
		logic = new ClientLogic( state );
		renderer = new GameRenderer( state );
		
		selectBoxRenderer = new ShapeRenderer();
		
		client = new DClient();
		client.register( ClassRegister.classes );
		client.register( SyncServer.registerClasses );
		
		//Direct messages
		client.on( DClient.CONNECTED, o -> {
			client.sendTCP( ClientMessages.JOIN );
		});
		
		client.on( ServerMessages.JOINSUCCESS, (Player p) -> {
			me = p;
		});
		
		//Sync handlers
		sync = new SyncClient( client );
		sync.onAddAndJoin( Map.class, map -> {
			state.map = map;
		});
		
		sync.onAddAndJoin( Player.class, p -> {
			state.players.add( p );
		});
		
		sync.onAddAndJoin( Unit.class, u -> {
			state.units.add( u );
		});
		
		sync.onAddAndJoin( Bullet.class, b -> {
			state.bullets.add( b );
		});
		
		sync.onRemove( Bullet.class, id -> {
			state.removeBullet( id );
		});
		
		try
		{
			client.connect( "localhost", GameServer.TCP_PORT, GameServer.UDP_PORT );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
		//UI
		ui.create();
		
		ui.addUnit.addListener( new ClickListener() {
			public void clicked( InputEvent e, float x, float y ) {
				client.sendTCP( ClientMessages.BUILDUNIT );
			}
		});
	}

	public void render( float dt )
	{
		client.update();
		logic.update( dt );
		
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
		
		//Render select box
		if( selecting )
		{
			selectBoxRenderer.setProjectionMatrix( camera.combined );
			selectBoxRenderer.begin( ShapeType.Line );
			selectBoxRenderer.setColor( 1, 0, 0, 1 );
			selectBoxRenderer.rect( selectStart.x, selectStart.y, selectEnd.x - selectStart.x, selectEnd.y - selectStart.y );
			selectBoxRenderer.end();
		}
		
		//Render UI
		ui.render();
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
			client.sendTCP( ClientMessages.MOVEUNITS, new Packets.MoveUnitPacket( new Vector2( projected.x, projected.y ), selected ) );
		}
		
		return true;
	}

	public boolean touchUp( int screenX, int screenY, int pointer, int button )
	{
		Vector3 projected = camera.unproject( new Vector3( screenX, screenY, 0 ) );
		selectEnd.set( projected.x, projected.y );
		
		selected = logic.getUnitIds( selectStart, selectEnd );
		
		selecting = false;
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
