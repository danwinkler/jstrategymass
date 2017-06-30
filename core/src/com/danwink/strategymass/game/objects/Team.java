package com.danwink.strategymass.game.objects;

import com.badlogic.gdx.graphics.Color;
import com.danwink.dsync.sync.SyncObject;

public class Team extends SyncObject<Team>
{
	public static Color[] colors = {
		new Color( 30/255.f, 167/255.f, 225/255.f, 1 ),
		new Color( 226/255.f, 121/255.f, 82/255.f, 1 ),
		Color.valueOf( "1B914DFF" ),
		Color.valueOf( "ACB8B8FF" ),
	};
	
	public int id;
		
	public Team() {}
	
	public Team( int id )
	{
		this.id = id;
	}

	public void set( Team so )
	{
		this.id = so.id;
	}
	
	public Color getColor()
	{
		return colors[id];
	}
}
