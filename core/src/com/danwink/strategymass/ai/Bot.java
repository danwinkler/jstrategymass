package com.danwink.strategymass.ai;

import com.danwink.strategymass.game.GameClient;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.net.DServer;
import com.danwink.strategymass.net.FakeClient;
import com.danwink.strategymass.nethelpers.ClientMessages;

public abstract class Bot implements Runnable
{
	GameClient c;
	Thread t;
	boolean running;
	long lastFrame;
	long targetNanosPerTick = 1000000000 / 3;
	
	public void connect( DServer server )
	{
		c = new GameClient( new FakeClient( server ) );
		c.start();
		
		t = new Thread( this );
		t.start();
	}

	public void run()
	{
		lastFrame = System.nanoTime();
		running = true;
		while( running ) {
			long now = System.nanoTime();
			long deltaTime = now - lastFrame;
			lastFrame = now;
			float dt = deltaTime / 1000000000.f;
			
			c.update( dt );
			update( c.me, c.state );
			
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
	}
	
	public abstract void update( Player me, GameState state );
	
	//BOT API
	public void send( Object key, Object value ) 
	{
		c.client.sendTCP( key, value );
	}
	
	public void buildUnit()
	{
		send( ClientMessages.BUILDUNIT, null );
	}
}
