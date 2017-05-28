package com.danwink.strategymass.net;

import java.util.ArrayList;
import java.util.Arrays;

import com.danwink.strategymass.nethelpers.ClientMessages;

public class SyncServer
{
	public static final String add = "SYNC.ADD";
	public static final String update = "SYNC.UPDATE";
	public static final String initial = "SYNC.INITIAL"; //Sent when syncobject isnt new, but when a user joins so they are seeing it for the first time
	public static final String partial = "SYNC.PARTIAL";
	public static final String remove = "SYNC.REMOVE";
	
	public static final Class[] registerClasses = new Class[] { SyncObject.class, PartialPacket.class, AddPacket.class };
	
	DServer server;
	
	int nextId = 0;
	
	ArrayList<SyncObject> syncies = new ArrayList<SyncObject>();
	
	public SyncServer( DServer server )
	{
		this.server = server;
		Arrays.asList( registerClasses ).forEach( c -> {
			server.server.getKryo().register( c );
		});
		
		server.on( ClientMessages.JOIN, (id, o) -> {
			for( int i = 0; i < syncies.size(); i++ )
			{
				SyncObject so = syncies.get( i );
				server.sendTCP( id, initial, new AddPacket( so.getClass().hashCode(), so ) );
			}
		});
	}
	
	public void add( SyncObject so )
	{
		so.syncId = nextId++;
		syncies.add( so );
		server.broadcastTCP( add, new AddPacket( so.getClass().hashCode(), so ) );
	}
	
	public void update()
	{
		for( int i = 0; i < syncies.size(); i++ )
		{
			SyncObject so = syncies.get( i );
			if( so.remove ) 
			{
				server.broadcastTCP( remove, so.syncId );
			} 
			else if( so.update ) 
			{
				if( so.partial ) 
				{
					server.broadcastTCP( partial, new PartialPacket( so.syncId, ((PartialUpdatable)so).partialMakePacket() ) );
				} 
				else 
				{
					server.broadcastTCP( update, so );
				}
			}
		}
	}
	
	public static class PartialPacket
	{
		int id;
		Object partial;
		
		public PartialPacket() {}
		
		public PartialPacket( int id, Object partial )
		{
			this.id = id;
			this.partial = partial;
		}
	}
	
	public static class AddPacket
	{
		int classHash;
		SyncObject object;
		
		public AddPacket() {}
		
		public AddPacket( int classHash, SyncObject object )
		{
			this.classHash = classHash;
			this.object = object;
		}
	}
}
