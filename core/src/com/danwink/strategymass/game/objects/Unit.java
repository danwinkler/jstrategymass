package com.danwink.strategymass.game.objects;

import java.util.ArrayList;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.game.GameLogic;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.net.SyncObject;

public class Unit extends SyncObject<Unit>
{
	public static final float radius = 16;
	public static final float speed = 4;
	public static final float shootInterval = 1;
	public static final int unitCost = 10;
	
	public int owner;
	public int team;
	public Vector2 pos;
	public float heading;
	public int health = 100;
	public float coolDown = 0;
	
	public int onPath = -1;
	public ArrayList<GridPoint2> path;
	public float targetX;
	public float targetY;
	
	public void set( Unit u )
	{
		this.owner = u.owner;
		this.team = u.team;
		this.pos = u.pos;
		this.heading = u.heading;
		this.onPath = u.onPath;
		this.path = u.path;
		this.health = u.health;
		this.coolDown = u.coolDown;
	}

	public void update( float dt, GameLogic logic, GameState state )
	{
		float dx = 0;
		float dy = 0;
		if( onPath != -1 )
		{
			if( onPath < path.size() )
			{
				GridPoint2 gp = path.get( onPath );
				float tx = (gp.x+.5f) * state.map.tileWidth;
				float ty = (gp.y+.5f) * state.map.tileHeight;
				
				dx += MathUtils.clamp( (tx - pos.x) * .5f, -speed, speed );
				dy += MathUtils.clamp( (ty - pos.y) * .5f, -speed, speed );
				
				int tileX = (int)(pos.x/state.map.tileWidth);
				int tileY = (int)(pos.y/state.map.tileHeight);
				if( tileX == gp.x && tileY == gp.y )
				{
					onPath++;
				}
			}
			else
			{
				dx += MathUtils.clamp( (targetX - pos.x) * .5f, -speed, speed );
				dy += MathUtils.clamp( (targetY - pos.y) * .5f, -speed, speed );
				
				if( MathUtils.isEqual( targetX, pos.x, 5 ) && MathUtils.isEqual( targetY, pos.y, 1 ) )
				{
					onPath = -1;
				}
			}
		}
		
		for( Unit u : state.units ) 
		{
			if( u.syncId == syncId ) continue;
			
			float udx = u.pos.x - pos.x;
			float udy = u.pos.y - pos.y;
			
			float d2 = udx*udx + udy*udy;
			if( Math.abs( d2 ) < Unit.radius*Unit.radius*1.5f*1.5f )
			{
				dx += MathUtils.clamp( -(1.f / d2) * udx, -1, 1 );
				dy += MathUtils.clamp( -(1.f / d2) * udy, -1, 1 );
			}
			
			if( u.team == this.team ) continue;
			if( coolDown > 0 ) continue;
			
			if( !Bullet.hitwall( pos.x, pos.y, udx, udy, state.map ) ) 
			{
				float heading = MathUtils.atan2( u.pos.y-pos.y, u.pos.x-pos.x );
				logic.shootBullet( this, heading );
				coolDown = shootInterval;
			}
		}
		
		if( dx != 0 || dy != 0 )
		{
			if( !state.map.isPassable( (int)((pos.x + dx) / state.map.tileWidth), (int)(pos.y/state.map.tileHeight) ) ) dx = 0;
			if( !state.map.isPassable( (int)(pos.x/state.map.tileWidth), (int)((pos.y+dy)/state.map.tileHeight) ) ) dy = 0;
			
			pos.x += dx;
			pos.y += dy;
			update = true;
		}
		
		if( health <= 0 )
		{
			remove = true;
		}
		
		if( coolDown > 0 ) 
		{
			coolDown -= dt;
		}
	}

	public boolean isMoving()
	{
		return onPath >= 0;
	}
}
