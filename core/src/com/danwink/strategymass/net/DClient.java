package com.danwink.strategymass.net;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class DClient
{
	public static final String CONNECTED = "net.CONNECTED";
	public static final String DISCONNECTED = "net.CONNECTED";
	
	boolean handleMessages = true;
	
	Client c;
	Thread clientThread;
	ConcurrentLinkedDeque<Message> messages = new  ConcurrentLinkedDeque<Message>();
	
	ListenerManager<ClientMessageListener> listenerManager;
	
	public DClient() 
	{
		listenerManager = new ListenerManager<>();
		
		c = new Client( 128000, 32000 );
		c.getKryo().register( Message.class );
		
		c.addListener( new Listener() {
			public void received( Connection c, Object o ) 
			{
				if( o instanceof Message )
				{
					synchronized( messages )
					{
						messages.addLast( (Message)o );
					}
				}
			}
			
			public void connected( Connection c )
			{
				messages.addLast( new Message( CONNECTED, null ) );
			}
			
			public void disconnected( Connection c )
			{
				
			}
		});
	}
	
	public void connect( String address, int tcpPort, int udpPort ) throws IOException 
	{
		c.start();
		c.connect( 2500, address, tcpPort, udpPort );	
	}
	
	public void update() throws IOException
	{
		c.update( 0 );
		if( handleMessages ) processMessages();
	}
	
	public void register( Class...classes )
	{
		for( Class cToR : classes ) 
		{
			c.getKryo().register( cToR );
		}
	}
	
	public void sendTCP( Object key, Object value ) 
	{
		c.sendTCP( new Message( key, value ) );
	}
	
	public Message getNextClientMessage()
	{	
		return messages.removeFirst();
	}
	

	public boolean hasClientMessages() 
	{
		return !messages.isEmpty();
	}

	public interface ClientMessageListener<E>
	{
		public void receive( E message );
	}
	
	@SuppressWarnings( { "unchecked" } )
	public void processMessages()
	{
		while( hasClientMessages() )
		{
			Message m = messages.removeFirst();
			listenerManager.call( m.key, l -> {
				l.receive( m.value );
			});
		}
	}
	
	public <E> void listen( Object key, ClientMessageListener<E> listener ) 
	{
		listenerManager.listen( key, listener );
	}
}
