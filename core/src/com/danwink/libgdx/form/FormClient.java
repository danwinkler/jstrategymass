package com.danwink.libgdx.form;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.danwink.dsync.DClient;

public class FormClient
{
	public static final String CHANGE = "com.danwink.form.CHANGE";
	
	DClient client;
	
	HashMap<Object, Actor> actors; 

	public FormClient( DClient client )
	{
		this( client, null );
	}
	
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public FormClient( DClient client, Object state )
	{
		this.client = client;
		
		actors = new HashMap<>();
		
		client.register( FormServer.registerClasses );
		
		client.on( state, FormServer.UPDATE, (FormMessage m) -> {
			updateActorFromMessage( actors.get( m.id ), m.m );
		});
	}
	
	public void add( Object id, Actor a )
	{
		actors.put( id, a );
		addListeners( id, a );
	}
	
	public void addListeners( Object id, Actor a )
	{
		if( a instanceof TextButton )
		{
			addTextButtonListener( id, (TextButton)a );
		}
	}
	
	public void addTextButtonListener( Object id, TextButton a )
	{
		a.addListener( new ChangeListener() {
			public void changed( ChangeEvent event, Actor actor )
			{
				client.sendTCP( CHANGE, id );
			}
		});
	}
	
	public void updateActorFromMessage( Actor a, Object m )
	{
		if( a instanceof TextButton )
		{
			((TextButton) a).setText( (String)m ); 
		}
	}
}
