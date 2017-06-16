package com.danwink.libgdx.form;

import java.util.HashMap;

import com.danwink.dsync.DServer;

public class FormServer
{	
	public static final String UPDATE = "com.danwink.FormServer.UPDATE";
	public static Class[] registerClasses = new Class[] {
		FormMessage.class,
		Object[].class,
		String[].class,
	};
	
	DServer server;
	
	HashMap<Object, SElement> children;
	
	public FormServer( DServer server )
	{
		this( server, null );
	}
	
	public FormServer( DServer server, Object state )
	{
		this.server = server;
		
		server.register( registerClasses );
		
		server.on( state, FormClient.CHANGE, (int id, FormMessage m) -> {
			change( id, children.get( m.id ), m.m );
		});
		
		children = new HashMap<>();
	}
	
	public void add( SElement child )
	{
		children.put( child.id, child );
	}
	
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public void change( int id, SElement e, Object m )
	{
		if( e instanceof STextButton )
		{
			STextButton t = (STextButton)e;
			t.click( id );
		}
		else if( e instanceof SSelectBox )
		{
			SSelectBox t = (SSelectBox)e;
			t.setSelected( m );
			t.change( id );
			update( t );
		}
	}

	public void update( SElement e )
	{
		FormMessage m = new FormMessage( e.id, e.serialize() );
		server.broadcastTCP( UPDATE, m );
	}

	public SElement get( String key )
	{
		return children.get( key );
	}

	public void updateClient( int id )
	{
		children.forEach( (o, e) -> {
			server.sendTCP( id, UPDATE, new FormMessage( e.id, e.serialize() ) );
		});
	}
}
