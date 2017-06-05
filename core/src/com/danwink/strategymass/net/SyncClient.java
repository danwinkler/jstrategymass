package com.danwink.strategymass.net;

import java.util.HashMap;

import com.danwink.strategymass.net.SyncServer.AddPacket;
import com.danwink.strategymass.net.SyncServer.PartialPacket;
import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;

public class SyncClient
{
	DClient client;
	@SuppressWarnings( "rawtypes" )
	ListenerManager<ObjectListener> addLm;
	
	@SuppressWarnings( "rawtypes" )
	ListenerManager<ObjectListener> initLm;
	
	ListenerManager<IdListener> removeLm;
	
	@SuppressWarnings( "rawtypes" )
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
		
		client.on( SyncServer.partial, (PartialPacket p) -> {
			PartialUpdatable pu = (PartialUpdatable)syncies.get( p.id );
			if( pu == null ) return;
			if( p.partial instanceof KeepAlive )
			{
				System.out.println( "A KEEP ALIVE WAS FOUND AS A PART OF A MESSAGE PACKET" );
				return;
			}
			pu.partialReadPacket( p.partial );
		});
		
		client.on( SyncServer.update, (SyncObject so) -> {
			SyncObject s = syncies.get( so.syncId );
			s.set( so );
		});
		
		client.on( SyncServer.remove, (Integer id) -> {
			SyncObject so = syncies.remove( id );
			so.remove = true;
			
			removeLm.call( so.getClass().getSimpleName().hashCode(), l -> {
				l.id( id );
			});
		});
	}
	
	public <E> void onAdd( Class<E> c, ObjectListener<E> listener )
	{
		addLm.on( c.getSimpleName().hashCode(), listener );
	}
	
	public <E> void onJoin( Class<E> c, ObjectListener<E> listener )
	{
		initLm.on( c.getSimpleName().hashCode(), listener );
	}
	
	public <E> void onAddAndJoin( Class<E> c, ObjectListener<E> listener )
	{
		addLm.on( c.getSimpleName().hashCode(), listener );
		initLm.on( c.getSimpleName().hashCode(), listener );
	}
	
	public <E> void onRemove( Class<E> c, IdListener listener )
	{
		removeLm.on( c.getSimpleName().hashCode(), listener );
	}
	
	public void clearListeners()
	{
		addLm.clear();
		initLm.clear();
		removeLm.clear();
	}
	
	public void remove( int id )
	{
		syncies.remove( id );
	}
	
	@SuppressWarnings( "rawtypes" )
	public SyncObject get( int id )
	{
		return syncies.get( id );
	}
	
	public interface ObjectListener<E>
	{
		public void object( E o );
	}
	
	public interface IdListener
	{
		public void id( int id );
	}
}
