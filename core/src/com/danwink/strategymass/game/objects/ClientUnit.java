package com.danwink.strategymass.game.objects;

import com.badlogic.gdx.math.MathUtils;
import com.danwink.strategymass.game.GameState;

public class ClientUnit implements UnitWrapper
{
	public Unit u;
	public float x, y;
	
	public ClientUnit( Unit u )
	{
		this.u = u;
		x = u.pos.x;
		y = u.pos.y;
	}
	
	public Unit getUnit()
	{
		return u;
	}
	
	public void update( float dt, GameState state )
	{
		u.move( dt, state );
		
		x = MathUtils.lerp( x, u.pos.x, .5f );
		y = MathUtils.lerp( y, u.pos.y, .5f );
	}
}
