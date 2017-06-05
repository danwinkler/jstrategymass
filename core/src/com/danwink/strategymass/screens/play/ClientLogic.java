package com.danwink.strategymass.screens.play;

import java.util.ArrayList;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.ClientUnit;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;

public class ClientLogic
{
	GameState state;
	
	public ClientLogic( GameState state )
	{
		this.state = state;
	}

	public ArrayList<Integer> getUnitIds( Vector2 a, Vector2 b, int owner )
	{
		ArrayList<Integer> units = new ArrayList<>();
		
		//Make sure a is bottom left and b is top right
		if( a.x > b.x ) {
			float c = a.x;
			a.x = b.x;
			b.x = c;
		}
		
		if( a.y > b.y ) {
			float c = a.y;
			a.y = b.y;
			b.y = c;
		}
		
		for( UnitWrapper uw : state.units )
		{
			Unit u = uw.getUnit();
			if( u.owner != owner ) continue;
			
			if( u.pos.x > a.x && u.pos.x < b.x && u.pos.y > a.y && u.pos.y < b.y )
			{
				units.add( u.syncId );
			}
		}
		
		return units;
	}
	
	public void update( float dt )
	{
		for( int i = 0; i < state.bullets.size(); i++ )
		{
			Bullet b = state.bullets.get( i );
			b.update( dt, state );
			if( !b.alive )
			{
				state.bullets.remove( i );
				i--;
			}
		}
		
		for( int i = 0; i < state.units.size(); i++ )
		{
			ClientUnit u = (ClientUnit)state.units.get( i );
			u.update( dt, state );
		}
		
		state.units.sort((a, b) -> {
			float af = ((ClientUnit)a).y;
			float bf = ((ClientUnit)b).y;
			if( af < bf ) return 1;
			else if( bf < af ) return -1;
			return 0;
		});
	}
}
