package com.danwink.strategymass.game.objects;

import java.util.ArrayList;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.game.GameLogic;
import com.danwink.strategymass.game.GameState;
import com.danwink.dsync.PartialUpdatable;
import com.danwink.dsync.sync.SyncObject;

public class Unit extends SyncObject<Unit> implements PartialUpdatable<UnitPartial>
{
	public static final float pushConstant = 30;
	public static final int unitCost = 10;
	public static final float partialTime = .3f;
	
	public int owner;
	public int team;
	public Vector2 pos;
	public float heading;
	public int health = 100;
	public float coolDown = 0;
	public float radius = 16;
	public float speed = 120;
	public float shootInterval = 1;
	
	public int onPath = -1;
	public ArrayList<GridPoint2> path;
	public float targetX;
	public float targetY;
	public float lastUpdate = 0;
	public int targetAbsorb = -1;
	public int absorbCount = 0;
	
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
		this.targetX = u.targetX;
		this.targetY = u.targetY;
		this.radius = u.radius;
		this.targetAbsorb = u.targetAbsorb;
		this.speed = u.speed;
		this.shootInterval = u.shootInterval;
	}
	
	public void move( float dt, GameState state )
	{
		Vector2 d = new Vector2();
		//Move along path
		if( onPath != -1 )
		{
			float speedDt = speed * dt;
			if( onPath < path.size() )
			{
				GridPoint2 gp = path.get( onPath );
				float tx = (gp.x+.5f) * state.map.tileWidth;
				float ty = (gp.y+.5f) * state.map.tileHeight;
				
				d.x += MathUtils.clamp( (tx - pos.x) * .5f, -speedDt, speedDt );
				d.y += MathUtils.clamp( (ty - pos.y) * .5f, -speedDt, speedDt );
				
				int tileX = MathUtils.floor( pos.x/state.map.tileWidth );
				int tileY = MathUtils.floor( pos.y/state.map.tileHeight );
				if( tileX == gp.x && tileY == gp.y )
				{
					update = true;
					onPath++;
				}
			}
			else
			{
				d.x += MathUtils.clamp( (targetX - pos.x) * .5f, -speedDt, speedDt );
				d.y += MathUtils.clamp( (targetY - pos.y) * .5f, -speedDt, speedDt );
				
				if( MathUtils.isEqual( targetX, pos.x, 5 ) && MathUtils.isEqual( targetY, pos.y, 5 ) )
				{
					onPath = -1;
					update = true;
					if( targetAbsorb >= 0 )
					{
						UnitWrapper target = state.unitMap.get( targetAbsorb );
						if( target != null )
						{
							remove = true;
							target.getUnit().absorbCount++;
						}
						else
						{
							targetAbsorb = -1;
						}
					}
				}
			}
		}
		
		//Repel other units
		float pushDt = pushConstant;
		for( UnitWrapper uw : state.units ) 
		{
			Unit u = uw.getUnit();
			if( u.syncId == syncId ) continue;
			
			float udx = u.pos.x - pos.x;
			float udy = u.pos.y - pos.y;
			
			float d2 = udx*udx + udy*udy;
			if( Math.abs( d2 ) < radius*radius*1.5f*1.5f )
			{
				d.x += MathUtils.clamp( -(60.f / d2) * udx, -pushDt, pushDt ) * dt;
				d.y += MathUtils.clamp( -(60.f / d2) * udy, -pushDt, pushDt ) * dt;
			}
		}
		
		//Move if we need to
		if( d.x != 0 || d.y != 0 )
		{
			//TODO: the current mapping of world to tile is broken for negative numbers
			if( pos.x + d.x < 0 ) d.x = 0;
			if( pos.y + d.y < 0 ) d.y = 0;
			if( !state.map.isPassable( MathUtils.floor((pos.x + d.x) / state.map.tileWidth), MathUtils.floor(pos.y/state.map.tileHeight) ) ) d.x = 0;
			if( !state.map.isPassable( MathUtils.floor(pos.x/state.map.tileWidth), MathUtils.floor((pos.y+d.y)/state.map.tileHeight) ) ) d.y = 0;
			
			d.limit( speed );
			
			pos.x += d.x;
			pos.y += d.y;
			if( lastUpdate <= 0 )
			{
				partial = true;
				lastUpdate = .5f;// partialTime;
			} 
			else 
			{
				lastUpdate -= dt;
			}
		}
	}
	
	public void setMove( Vector2 pos, ArrayList<GridPoint2> path )
	{
		this.path = path;
		this.onPath = 0;	
		this.targetX = pos.x;
		this.targetY = pos.y;
		this.update = true;
		this.absorbCount = 0;
		this.targetAbsorb = -1;
	}

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
				logic.shootBullet( this, heading );
				coolDown = shootInterval;
			}
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
	
	public void partialReadPacket( UnitPartial e )
	{
		pos.set( e.x, e.y );
		onPath = e.onPath;
	}

	public UnitPartial partialMakePacket()
	{
		UnitPartial up = new UnitPartial();
		up.x = pos.x;
		up.y = pos.y;
		up.onPath = onPath;
		return up;
	}
}
