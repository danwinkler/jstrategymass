package com.danwink.strategymass.screens.play;

import java.util.ArrayList;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.AudioManager;
import com.danwink.strategymass.AudioManager.GameSound;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.ClientUnit;
import com.danwink.strategymass.game.objects.RegularUnit;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;

public class ClientLogic
{
	GameState state;
	public boolean isBot = false;
	
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
	
	public ArrayList<Integer> getUnitIds( Vector2 a, int owner )
	{
		ArrayList<Integer> units = new ArrayList<>();
		
		Unit closest = null;
		float distance = 100000000;
		for( UnitWrapper uw : state.units )
		{
			Unit u = uw.getUnit();
			if( u.owner != owner ) continue;
			
			float d = u.pos.dst2( a ); 
			if( d < distance && d < u.radius*u.radius )
			{
				closest = u;
				distance = d;
			}
		}
		
		if( closest != null )
		{
			units.add( closest.syncId );
		}
		
		return units;
	}
	
	public ArrayList<Integer> getVisibleUnitsOfType( Vector2 min, Vector2 max, int owner, Class<? extends Unit> type )
	{
		ArrayList<Integer> units = new ArrayList<>();
		
		for( UnitWrapper uw : state.units )
		{
			Unit u = uw.getUnit();
			if( u.owner != owner ) continue;
			
			if( type.isInstance( u ) && u.pos.x > min.x && u.pos.x < max.x && u.pos.y > min.y && u.pos.y < max.y )
			{
				units.add( u.syncId );
			}
		}
		
		return units;
	}
	
	public boolean canCombine( ArrayList<Integer> ids )
	{
		int count = 0;
		
		for( Integer id : ids )
		{
			Unit u = state.unitMap.get( id ).getUnit();
			if( u instanceof RegularUnit )
			{
				count++;
				if( count >= 10 )
				{
					return true;
				}
			}
			
		}
		
		return false;
	}
	
	public void update( float dt )
	{
		for( int i = 0; i < state.bullets.size(); i++ )
		{
			Bullet b = state.bullets.get( i );
			int ret = b.update( dt, state );
			if( !isBot )
			{
				switch( ret )
				{
				case 0: break;
				case 1: break; // Hit wall
				case 2: AudioManager.play( GameSound.UNIT_HIT, b.pos ); break; //Hit unit
				}
			}
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
