package com.danwink.strategymass.game.objects;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.net.SyncObject;

public class Bullet extends SyncObject<Bullet>
{
	public static float SPEED = 400;
	
	public Vector2 pos;
	public float heading;
	public int team;
	public boolean alive = true; //This is necessary so on client we can remove ourselves
	public int damage = 30;
	public float life = -100;
	
	public Bullet() {}
	
	public Bullet( Vector2 pos, float heading )
	{
		this.pos = pos;
		this.heading = heading;
	}

	public void set( Bullet so )
	{
		this.pos = so.pos;
		this.heading = so.heading;
		this.team = so.team;
		this.damage = so.damage;
		this.life = so.life;
	}

	public void update( float dt, GameState state )
	{
		float sx = pos.x;
		float sy = pos.y;
		
		move( dt, state );
		
		for( UnitWrapper uw : state.units )
		{
			Unit u = uw.getUnit();
			if( u.team == this.team ) continue;
			
			if( Intersector.distanceSegmentPoint( sx, sy, pos.x, pos.y, u.pos.x, u.pos.y ) < Unit.radius )
			{
				u.health -= this.damage;
				this.alive = false;
				this.remove = true;
				return;
			}
		}
	}

	public void move( float dt, GameState state )
	{
		float dx = MathUtils.cos( heading ) * dt * SPEED;
		float dy = MathUtils.sin( heading ) * dt * SPEED;  
		
		if( life == -100 )
		{
			Vector2 result = new Vector2();
			hitwall( this.pos.x, this.pos.y, dx * 1000, dy * 1000, state.map, result );
			result.sub( this.pos );
			life = result.len() / SPEED;
		}
		
		pos.x += dx;
		pos.y += dy;
		life -= dt;
		
		if( life <= 0 ) 
		{
			remove = true;
			alive = false;
		}
	}
	
	public static boolean hitwall( float x, float y, float dx, float dy, Map map )
	{
		return hitwall( x, y, dx, dy, map, null );
	}
	
	public static boolean hitwall( float x, float y, float dx, float dy, Map map, Vector2 result )
	{
		int cx, cy; // current x, y, in tiles
		float cbx, cby; // starting tile cell bounds, in pixels
		float tMaxX, tMaxY; // maximum time the ray has traveled so far (not
							// distance!)
		float tDeltaX = 0, tDeltaY = 0; // the time that the ray needs to travel
										// to cross a single tile (not
										// distance!)
		int stepX, stepY; // step direction, either 1 or -1
		float outX, outY; // bounds of the tileMap where the ray would exit
		boolean hitTile = false;
		float tResult = 0;
		
		Vector2 direction = new Vector2( dx, dy );
		
		// find the tile at the start position of the ray
		cx = (int)(x / map.tileWidth);
		cy = (int)(y / map.tileHeight);
		
		if( cx < 0 || cx >= map.width || cy < 0 || cy >= map.height )
		{
			// outside of the tilemap
			//result.x = start.x;
			//result.y = start.y;
			return true;
		}
		
		if( !map.isPassable( cx, cy ) )
		{
			// start point is inside a block
			//result.x = start.x;
			//result.y = start.y;
			return true;
		}
		
		int maxTilesToCheck = map.height * map.width;
		
		// determine step direction, and initial starting block
		if( direction.x > 0 )
		{
			stepX = 1;
			outX = map.width;
			cbx = (cx + 1) * map.tileWidth;
		}
		else
		{
			stepX = -1;
			outX = -1;
			cbx = cx * map.tileWidth;
		}
		if( direction.y > 0 )
		{
			stepY = 1;
			outY = map.height;
			cby = (cy + 1) * map.tileHeight;
		}
		else
		{
			stepY = -1;
			outY = -1;
			cby = cy * map.tileHeight;
		}
		
		// determine tMaxes and deltas
		if( direction.x != 0 )
		{
			tMaxX = (cbx - x) / direction.x;
			tDeltaX = map.tileWidth * stepX / direction.x;
		}
		else tMaxX = 1000000;
		if( direction.y != 0 )
		{
			tMaxY = (cby - y) / direction.y;
			tDeltaY = map.tileHeight * stepY / direction.y;
		}
		else tMaxY = 1000000;
		
		// step through each block
		for( int tileCount = 0; tileCount < maxTilesToCheck; tileCount++ )
		{
			if( tMaxX < tMaxY )
			{
				cx = cx + stepX;
				if( !map.isPassable( cx, cy ) )
				{
					hitTile = true;
					break;
				}
				if( cx == outX )
				{
					hitTile = false;
					break;
				}
				tMaxX = tMaxX + tDeltaX;
			}
			else
			{
				cy = cy + stepY;
				if( !map.isPassable( cx, cy ) )
				{
					hitTile = true;
					break;
				}
				if( cy == outY )
				{
					hitTile = false;
					break;
				}
				tMaxY = tMaxY + tDeltaY;
			}
		}
		
		// result time
		tResult = (tMaxX < tMaxY) ? tMaxX : tMaxY;
		
		
		// store the result
		if( result != null )
		{
			result.x = x + (direction.x * tResult);
			result.y = y + (direction.y * tResult);
		}
		
		/*
		if( resultInTiles != null ) { 
			resultInTiles.x = cx; 
			resultInTiles.y = cy; 
		}
		*/
	
		return hitTile && tResult < 1;
	}
}
