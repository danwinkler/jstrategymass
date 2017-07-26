package com.danwink.strategymass.game.objects;

import com.danwink.strategymass.game.GameLogic;
import com.danwink.strategymass.game.GameState;

public class ServerUnit implements UnitWrapper
{
	public Unit u;
	
	public ServerUnit( Unit u )
	{
		this.u = u;
	}

	public Unit getUnit()
	{
		return u;
	}

	public void update( float dt, GameLogic logic, GameState state )
	{
		u.move( dt, state );
		u.shoot( dt, logic, state );
		
		if( u.absorbCount >= 9 )
		{
			u.remove = true;
			logic.buildMegaUnit( u.owner, u.pos );
		}
		
		if( u.health <= 0 )
		{
			u.remove = true;
		}
	}
}
