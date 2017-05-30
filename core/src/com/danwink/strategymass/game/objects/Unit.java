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
	public int owner;
	public int team;
	public Vector2 pos;
	public float heading;
	
	public int onPath = -1;
	public ArrayList<GridPoint2> path;
	
	public void set( Unit u )
	{
		this.owner = u.owner;
		this.team = u.team;
		this.pos = u.pos;
		this.heading = u.heading;
		this.onPath = u.onPath;
		this.path = u.path;
	}

	public void update( float dt, GameLogic logic, GameState state )
	{
		if( onPath != -1 )
		{
			GridPoint2 gp = path.get( onPath );
			float tx = (gp.x+.5f) * state.map.tileWidth;
			float ty = (gp.y+.5f) * state.map.tileHeight;
			
			pos.x += MathUtils.clamp( (tx - pos.x) * .5f, -3, 3 );
			pos.y += MathUtils.clamp( (ty - pos.y) * .5f, -3, 3 );
			
			int tileX = (int)(pos.x/state.map.tileWidth);
			int tileY = (int)(pos.y/state.map.tileHeight);
			if( tileX == gp.x && tileY == gp.y )
			{
				onPath++;
				if( onPath >= path.size() ) 
				{
					onPath = -1;
				}
			}
			
			update = true;
		}
		
		for( Unit u : state.units ) 
		{
			if( u.team == this.team ) continue;
			
			float heading = MathUtils.atan2( u.pos.y-pos.y, u.pos.x-pos.x );
			logic.shootBullet( this, heading );
		}
	}
}
