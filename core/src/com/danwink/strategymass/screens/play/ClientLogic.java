package com.danwink.strategymass.screens.play;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.Unit;

public class ClientLogic
{
	GameState state;
	
	public ClientLogic( GameState state )
	{
		this.state = state;
	}

	public ArrayList<Integer> getUnitIds( Vector2 a, Vector2 b )
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
		
		for( Unit u : state.units )
		{
			if( u.pos.x > a.x && u.pos.x < b.x && u.pos.y > a.y && u.pos.y < b.y )
			{
				units.add( u.syncId );
			}
		}
		
		return units;
	}
	
	public void update( float dt )
	{
		for( Bullet b : state.bullets )
		{
			b.update( dt, state );
		}
	}
}
