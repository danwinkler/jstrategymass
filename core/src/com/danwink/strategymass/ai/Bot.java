package com.danwink.strategymass.ai;

import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.game.GameClient;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.MapPathFinding.MapGraph;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;
import com.danwink.dsync.DServer;
import com.danwink.dsync.FakeClient;
import com.danwink.dsync.SyncServer;
import com.danwink.strategymass.nethelpers.ClassRegister;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.Packets;

public abstract class Bot implements Runnable
{
	GameClient c;
	Thread t;
	boolean running;
	long lastFrame;
	long targetNanosPerTick = 1000000000 / 3;
	public int team = 0;
	public String name = "BOT";
	public int key;
	
	public void connect( DServer server )
	{
		FakeClient fc = new FakeClient( server );
		fc.register( ClassRegister.classes );
		fc.register( SyncServer.registerClasses );
		
		c = new GameClient();
		c.register( fc );
		c.team = team;
		c.name = name;
		c.start();
		
		try
		{
			fc.connect( null, 0, 0 );
		} 
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
		t = new Thread( this );
		t.setName( "Bot" );
		t.start();
		
		fc.sendTCP( ClientMessages.JOIN, key );
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
			if( c.gameOver ) {
				reset();
			}
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
	
	public abstract void reset();
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
	
	public void moveUnit( Unit u, float x, float y )
	{
		ArrayList<Integer> selected = new ArrayList<>();
		selected.add( u.syncId );
		send( ClientMessages.MOVEUNITS, new Packets.MoveUnitPacket( new Vector2( x, y ), selected ) );
	}
	
	//Helper Functions
	public Point findPointShortestPath( float x, float y, MapGraph graph, Filter<Point> f )
	{
		Point closeb = null;
		float closeDist = Float.MAX_VALUE;
		for( Point b : c.state.map.points )
		{
			if( f.valid( b ) )
			{
				GridPoint2 adj = b.findAjacent( c.state.map );
				ArrayList<GridPoint2> path = graph.search( 
					adj.x, 
					adj.y, 
					(int)(x/c.state.map.tileWidth), 
					(int)(y/c.state.map.tileHeight) 
				);
				
				if( path == null ) continue;
				float d2 = path.size();
				if( d2 < closeDist )
				{
					closeDist = d2;
					closeb = b;
				}
			}
		}
		return closeb;
	}
	
	public int numUnitsAtPoint( Point p )
	{
		int count = 0;
		for( UnitWrapper uw : c.state.units )
		{
			Unit u = uw.getUnit();
			if( p.isHere( u.pos, c.state ) )
			{
				count++;
			}
		}
		return count;
	}
	
	public void stop()
	{
		running = false;
	}
	
	public interface Filter<E>
	{
		public boolean valid( E e );
	}
}
