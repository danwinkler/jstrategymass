package com.danwink.strategymass.net;

import com.danwink.strategymass.net.SyncServer.AddPacket;

public class SyncClient
{
	DClient client;
	ListenerManager<ObjectListener> addLm;
	ListenerManager<ObjectListener> initLm;
	
	@SuppressWarnings( "unchecked" )
	public SyncClient( DClient client )
	{
		this.client = client;
		
		addLm = new ListenerManager<>();
		initLm = new ListenerManager<>();
		
		client.listen( SyncServer.add, (AddPacket p) -> {
			addLm.call( p.classHash, l -> {
				l.add( p.object );
			});
		});
		
		client.listen( SyncServer.initial, (AddPacket p) -> {
			addLm.call( p.classHash, l -> {
				l.add( p.object );
			});
		});
	}
	
	public <E> void onAdd( Class<E> c, ObjectListener<E> listener )
	{
		addLm.listen( c.hashCode(), listener );
	}
	
	public <E> void onJoin( Class<E> c, ObjectListener<E> listener )
	{
		initLm.listen( c.hashCode(), listener );
	}
	
	public <E> void onAddAndJoin( Class<E> c, ObjectListener<E> listener )
	{
		addLm.listen( c.hashCode(), listener );
		initLm.listen( c.hashCode(), listener );
	}
	
	public interface ObjectListener<E>
	{
		public void add( E o );
	}
}
