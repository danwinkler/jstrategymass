package com.danwink.strategymass.screens.play;

import java.io.IOException;

import com.badlogic.gdx.Screen;
import com.danwink.dsync.DClient;
import com.danwink.dsync.DEndPoint;
import com.danwink.strategymass.Screens;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.nethelpers.ClassRegister;
import com.danwink.strategymass.server.GameServer;
import com.danwink.strategymass.server.ServerState;

public class ConnectedScreen implements Screen
{	
	Screen active;
	
	String address = "127.0.0.1";
	DClient client;
		
	public void setScreen( Screen s )
	{
		if( active != null ) active.dispose();
		active = s;
		active.show();
	}
	
	public void setAddress( String address )
	{
		this.address = address;
	}
	
	public void show()
	{
		client = new DClient();
		client.register( ClassRegister.classes );
		
		Screens.connecting.register( client );
		Screens.lobby.register( client );
		Screens.play.register( client );
		
		client.on( DEndPoint.SET_STATE, (ServerState state) -> {
			switch( state )
			{
			case LOBBY:
				setScreen( Screens.lobby );
				break;
			case PLAY:
				setScreen( Screens.play );
				break;
			default:
				break;
			}
		});
		
		try
		{
			client.connect( address, GameServer.TCP_PORT, GameServer.UDP_PORT );
		} 
		catch( IOException e )
		{
			StrategyMass.game.setScreen( Screens.mainMenu );
		}
		
		setScreen( Screens.connecting );
	}

	public void render( float delta )
	{
		active.render( delta );
	}

	public void resize( int width, int height )
	{
		active.resize( width, height );
	}

	public void pause()
	{
		active.pause();
	}

	public void resume()
	{
		active.resume();
	}

	public void hide()
	{
		active.hide();
	}

	public void dispose()
	{
		active.dispose();
	}
}
