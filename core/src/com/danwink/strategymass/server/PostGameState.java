package com.danwink.strategymass.server;

import com.danwink.dsync.DServer;
import com.danwink.libgdx.form.FormServer;
import com.danwink.libgdx.form.STextButton;

public class PostGameState implements com.danwink.dsync.ServerState
{	
	DServer server;

	FormServer fs;
	
	public void register( DServer server )
	{
		this.server = server;
		
		fs = new FormServer( server, ServerState.POSTGAME );
		
		fs.add( new STextButton( "next" ) {
			public void click( int id )
			{
				server.setState( ServerState.LOBBY );
			}
		});
	}
	
	public void show()
	{
		
	}

	public void update( float dt )
	{
		
	}

	public void hide()
	{
		
	}
}
