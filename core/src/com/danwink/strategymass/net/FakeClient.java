package com.danwink.strategymass.net;

import java.io.IOException;

public class FakeClient extends DClient
{
	DServer server;
	FakeConnection conn;

	public FakeClient( DServer server )
	{
		listenerManager = new ListenerManager<>();
		
		this.server = server;
		conn = new FakeConnection( this, server.server.getKryo() );
	}
	
	@Override
	public void connect( String address, int tcpPort, int udpPort ) throws IOException 
	{
		//Manually trigger methods on server and client
		server.connected( conn );
		connected( null );
	}
	
	@Override
	public void sendTCP( Object key, Object value ) 
	{
		server.messages.addLast( new Message( key, value, conn.id ) );
	}
	
	@Override
	public void sendTCP( Object key ) 
	{
		sendTCP( key, null );
	} 
}
