package com.danwink.strategymass.net;

import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class FakeConnection extends Connection
{
	public int id = MathUtils.random( 50, Integer.MAX_VALUE );
	public Listener l;
	public Kryo k;
	
	public FakeConnection( Listener l, Kryo k )
	{
		this.l = l;
		this.k = k;
	}
	
	public int getID()
	{
		return id;
	}
	
	public int sendTCP( Object o )
	{
		l.received( this, k.copy( o ) );
		return 0;
	}
}
