package com.danwink.strategymass;

import java.io.IOException;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.danwink.strategymass.gameobjects.Map;
import com.danwink.strategymass.net.DClient;
import com.danwink.strategymass.net.SyncClient;
import com.danwink.strategymass.net.SyncServer;
import com.danwink.strategymass.nethelpers.ClassRegister;
import com.danwink.strategymass.nethelpers.ClientMessages;

public class Play implements Screen
{
	StrategyMass game;
	
	OrthographicCamera camera;
	
	DClient client;
	SyncClient sync;
	
	GameState state;
	
	public Play( StrategyMass game )
	{
		this.game = game;
		
		camera = new OrthographicCamera();
		camera.setToOrtho( false );
	}

	public void show()
	{
		state = new GameState();
		
		client = new DClient();
		client.register( ClassRegister.classes );
		client.register( SyncServer.registerClasses );
		
		sync = new SyncClient( client );
		sync.onAddAndJoin( Map.class, map -> {
			System.out.println( "got map" );
			state.map = map;
		});
		
		client.listen( DClient.CONNECTED, o -> {
			client.sendTCP( ClientMessages.JOIN, null );
		});
		
		try
		{
			client.connect( "localhost", GameServer.TCP_PORT, GameServer.UDP_PORT );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	public void render( float delta )
	{
		try
		{
			client.update();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	public void resize( int width, int height )
	{
		
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
	
	}
}
