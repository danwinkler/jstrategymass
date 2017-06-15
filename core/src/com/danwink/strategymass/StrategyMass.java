package com.danwink.strategymass;

import java.time.LocalDateTime;
import java.util.Arrays;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.danwink.strategymass.server.GameServer;
import com.danwink.strategymass.server.ServerState;
import com.kotcrab.vis.ui.VisUI;

public class StrategyMass extends Game
{
	public static StrategyMass game;
	
	public GameServer server;
	
	public void create()
	{	
		Thread.setDefaultUncaughtExceptionHandler( (t, e) -> {
			e.printStackTrace();
			FileHandle f = Gdx.files.local( "error.log" );
			String stackTrace = Arrays.asList( e.getStackTrace() )
				.stream()
				.map( ste -> ste.toString() )
				.reduce( "", (a,b) -> a + "\n" + b );
			f.writeString( LocalDateTime.now().toString() + "\n" + e.getMessage() + "\n" + stackTrace + "\n\n", true );
			server.stop();
			System.exit( 1 );
		});
		
		VisUI.load();
		
		game = this;
		
		setScreen( Screens.mainMenu );
	}
	
	public void render()
	{
		super.render();
	}
	
	public void dispose()
	{
		
	}
	
	public static Preferences getPrefs( String name )
	{
		return Gdx.app.getPreferences( "com.danwink.strategymass." + name );
	}
	
	public static Preferences getSettings()
	{
		return Gdx.app.getPreferences( "com.danwink.strategymass.settings" );
	}
}
