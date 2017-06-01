package com.danwink.strategymass.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class DServer extends Listener
{
	public static final int WRITE_BUFFER = 5000000;
	public static final int OBJECT_BUFFER = 32000;
	public static final String CONNECTED = "net.CONNECTED";
	public static final String DISCONNECTED = "net.CONNECTED";
	
	Thread messageSenderThread;
	ThreadRunner tr;
	boolean running;
	
	boolean handleMessages = true;
	
	public ConcurrentLinkedDeque<Message> messages = new  ConcurrentLinkedDeque<Message>();
	ConcurrentLinkedDeque<MessagePacket> messagesToSend = new  ConcurrentLinkedDeque<MessagePacket>();
	HashMap<Integer, Connection> connections = new HashMap<Integer, Connection>();
	ArrayList<Connection> connectionsArr = new ArrayList<Connection>();
	
	Pool<MessagePacket> mpPool = Pools.get( MessagePacket.class );
	
	ListenerManager<ServerMessageListener> listenerManager;
	
	Server server;
	
	public DServer()
	{
		listenerManager = new ListenerManager<>();
		
		server = new Server( WRITE_BUFFER, OBJECT_BUFFER );
		server.getKryo().register( Message.class );
		server.addListener( this );
	}
	
	//Always needs to be called to bind the server to the ports
	public void start( int tcpPort, int udpPort ) throws IOException
	{
		running = true;
		
		server.bind( tcpPort, udpPort );
		server.start();
		
		messageSenderThread = new Thread( new MessageSender() ) ;
		messageSenderThread.start();
	}
	
	//Creates a server game loop (optional)
	public void startThread( Updateable u, int fps ) 
	{	
		tr = new ThreadRunner( fps );
		tr.start( u );
	}
	
	public Message getNextServerMessage()
	{	
		return messages.removeFirst();
	}

	public boolean hasServerMessages() 
	{
		return !messages.isEmpty();
	}
	
	public void sendTCP( int id, Object key, Object value )
	{
		MessagePacket mp = mpPool.obtain();
		mp.init( id, key, value );
		messagesToSend.addLast( mp );
	}
	
	public void broadcastTCP( Object key, Object value )
	{
		synchronized( connectionsArr ) 
		{
			for( int i = 0; i < connectionsArr.size(); i++ )
			{
				sendTCP( connectionsArr.get( i ).getID(), key, value );
			}
		}
	}
	
	private class ThreadRunner 
	{
		int framesPerSecond;
		Thread t;
		long lastFrame;
		long targetNanosPerTick;
		
		public ThreadRunner( int framesPerSecond ) 
		{
			this.framesPerSecond = framesPerSecond;
			targetNanosPerTick = 1000000000 / framesPerSecond;
		}
		
		public void start( Updateable u ) 
		{
			t = new Thread(()->{
				lastFrame = System.nanoTime();
				while( running ) {
					long now = System.nanoTime();
					long deltaTime = now - lastFrame;
					lastFrame = now;
					float dt = deltaTime / 1000000000.f;
					
					try
					{
						if( handleMessages ) processMessages();
					}
					catch( Exception e1 )
					{
						e1.printStackTrace();
					}
					
					u.update( dt );
					
					long remaining = targetNanosPerTick - (System.nanoTime() - now);
					if( remaining < 0 ) continue;
					try
					{
						Thread.sleep( remaining / 1000000 );
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}
			});
			t.start();
		}
	}
	
	public interface Updateable
	{
		public void update( float dt );
	}
	
	public static class MessagePacket implements Poolable
	{
		int destination;
		Message m;
		
		public MessagePacket()
		{
			destination = -1;
			m = new Message();
		}
		
		public void init( int destination, Object key, Object value )
		{
			this.destination = destination;
			m.key = key;
			m.value = value;
		}
		
		public void reset()
		{
			m.key = null;
			m.value = null;
			this.destination = -1;
		}
	}
	
	//Key Callback helpers
	
	public interface ServerMessageListener<E>
	{
		public void receive( int id, E e );
	}
	
	@SuppressWarnings( { "unchecked" } )
	public void processMessages()
	{
		while( hasServerMessages() )
		{
			Message m = messages.removeFirst();
			listenerManager.call( m.key, l -> {
				l.receive( m.sender, m.value );
			});
		}
	}
	
	public <E> void on( Object key, ServerMessageListener<E> listener ) 
	{
		listenerManager.on( key, listener );
	}
	
	public void register( Class...classes )
	{
		for( Class cToR : classes ) 
		{
			server.getKryo().register( cToR );
		}
	}
	
	//Kryonet Server Listeners
	
	public void received( Connection c, Object o ) 
	{
		if( o instanceof Message )
		{
			Message m = (Message)o;
			m.sender = c.getID();
			messages.addLast( m );
		}
	}
	
	public void connected( Connection c )
	{
		synchronized( connectionsArr )
		{
			Message m = new Message();
			m.value = CONNECTED;
			m.sender = c.getID();
			messages.push( m );
			if( connections.get( c.getID() ) == null )
			{
				connections.put( c.getID(), c );
				connectionsArr.add( c );
			}
		}
	}
	
	public void disconnected( Connection c )
	{
		synchronized( connectionsArr )
		{
			Message m = new Message();
			m.key = DISCONNECTED;
			m.sender = c.getID();
			messages.push( m );
			connections.remove( c.getID() );
			connectionsArr.remove( c );
		}
	}
	
	public class MessageSender implements Runnable
	{
		public void run()
		{
			while( running )
			{
				Iterator<MessagePacket> i = messagesToSend.iterator();
				while( i.hasNext() )
				{
					MessagePacket packet = i.next();
					synchronized( connections )
					{
						Connection c = connections.get( packet.destination );
						if( c == null )
						{
							i.remove();
							continue;
						}
						int bytes = 0;
						
						try 
						{
							bytes = c.getTcpWriteBufferSize();
						}
						catch( NullPointerException e ){}
						
						if( bytes < WRITE_BUFFER - OBJECT_BUFFER ) 
						{
							c.sendTCP( packet.m );
							i.remove();
						}
					}
				}
				
				try
				{
					Thread.sleep( 10 );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void stop()
	{
		server.stop();
		running = false;
	}
}
