package com.danwink.strategymass.net;

import java.util.HashMap;

import com.danwink.strategymass.net.SyncServer.AddPacket;
import com.esotericsoftware.reflectasm.FieldAccess;

public class SyncClient
{
	DClient client;
	ListenerManager<ObjectListener> addLm;
	ListenerManager<ObjectListener> initLm;
	ListenerManager<ObjectListener> removeLm;
	HashMap<Integer, SyncObject> syncies;
	
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public SyncClient( DClient client )
	{
		this.client = client;
		
		addLm = new ListenerManager<>();
		initLm = new ListenerManager<>();
		removeLm = new ListenerManager<>();
		
		syncies = new HashMap<>();
		
		client.on( SyncServer.add, (AddPacket p) -> {
			syncies.put( p.object.syncId, p.object );
			addLm.call( p.classHash, l -> {
				l.object( p.object );
			});
		});
		
		client.on( SyncServer.initial, (AddPacket p) -> {
			syncies.put( p.object.syncId, p.object );
			addLm.call( p.classHash, l -> {
				l.object( p.object );
			});
		});
		
		client.on( SyncServer.update, (SyncObject so) -> {
			SyncObject s = syncies.get( so.syncId );
			s.set( so );
		});
		
		client.on( SyncServer.remove, (Integer id) -> {
			SyncObject so = syncies.remove( id );
			so.remove = true;
			
			addLm.call( so.getClass().hashCode(), l -> {
				l.object( so );
			});
		});
	}
	
	public <E> void onAdd( Class<E> c, ObjectListener<E> listener )
	{
		addLm.on( c.hashCode(), listener );
	}
	
	public <E> void onJoin( Class<E> c, ObjectListener<E> listener )
	{
		initLm.on( c.hashCode(), listener );
	}
	
	public <E> void onAddAndJoin( Class<E> c, ObjectListener<E> listener )
	{
		addLm.on( c.hashCode(), listener );
		initLm.on( c.hashCode(), listener );
	}
	
	public <E> void onRemove( Class<E> c, ObjectListener<E> listener )
	{
		
	}
	
	public interface ObjectListener<E>
	{
		public void object( E o );
	}
}
