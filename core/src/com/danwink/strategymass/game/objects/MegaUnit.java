package com.danwink.strategymass.game.objects;

import com.badlogic.gdx.math.MathUtils;
import com.danwink.strategymass.game.GameLogic;
import com.danwink.strategymass.game.GameState;

public class MegaUnit extends Unit
{
	public MegaUnit()
	{
		radius = 24;
		speed = 80;
		health = 800;
		shootInterval = 2;
	}
	
	@Override
	public void shoot( float dt, GameLogic logic, GameState state )
	{
		for( UnitWrapper uw : state.units ) 
		{
			Unit u = uw.getUnit();
			float udx = u.pos.x - pos.x;
			float udy = u.pos.y - pos.y;
			
			if( u.team == this.team ) continue;
			if( coolDown > 0 ) continue;
			
			if( !Bullet.hitwall( pos.x, pos.y, udx, udy, state.map ) ) 
			{
				float heading = MathUtils.atan2( u.pos.y-pos.y, u.pos.x-pos.x );
				logic.shootBullet( this, heading, 100, false );
				coolDown = shootInterval;
			}
		}
		
		if( coolDown > 0 ) 
		{
			coolDown -= dt;
		}
	}
}
