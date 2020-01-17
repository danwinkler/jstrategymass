package com.danwink.strategymass.game.objects;

import com.danwink.strategymass.GridBucket;
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

	public void update( float dt, GameLogic logic, GameState state, GridBucket<UnitWrapper> gridBucket )
	{
		u.move( dt, state, gridBucket );
		u.shoot( dt, logic, state );
		
		// -1 because we count ourselves
		if( u.absorbCount >= MegaUnit.NUM_UNITS_TO_CREATE-1 )
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
