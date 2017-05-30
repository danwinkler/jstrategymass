package com.danwink.strategymass.net;

import java.util.concurrent.ConcurrentLinkedDeque;

import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class FakeConnection extends Connection
{
	public int id = MathUtils.random( 50, Integer.MAX_VALUE );
	public Listener l;
	
	public FakeConnection( Listener l )
	{
		this.l = l;
	}
	
	public int getID()
	{
		return id;
	}
	
	public int sendTCP( Object o )
	{
		l.received( this, o );
		return 0;
	}
}
