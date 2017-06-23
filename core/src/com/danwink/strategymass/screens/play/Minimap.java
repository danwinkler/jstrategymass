package com.danwink.strategymass.screens.play;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.danwink.strategymass.Assets;
import com.danwink.strategymass.game.objects.Map;

public class Minimap extends Actor
{
	float width = 200, height = 200;
	public Map m;
	public Texture tree, grass;
	
	float xo, yo, inc;
	
	public Minimap()
	{
		super();
		this.setSize( width, height );
		
		tree = Assets.getT( "tree_b" );
		grass = Assets.getT( "grass_a" );
	}
	
	public void setMap( Map m )
	{
		this.m = m;
		computeSize();
	}
	
	private void computeSize()
	{
		if( m.width >= m.height )
		{
			inc = width / m.width;
			xo = 0;
			yo = (inc * m.height) / 2f;
		}
		else
		{
			inc = height / m.height;
			xo = (inc * m.width) / 2f;
			yo = 0;
		}
	}
	
	public void draw( Batch batch, float parentActor )
	{
		SpriteBatch b = (SpriteBatch)batch;
		
		if( m == null ) return;
		
		for( int y = 0; y < m.height; y++ )
		{
			for( int x = 0; x < m.width; x++ )
			{
				int tId = m.tiles[y][x];
				batch.draw( 
					tId == Map.TILE_TREE ? tree : grass, 
					getX() + x*inc + xo, 
					getY() + y*inc + yo,
					inc, 
					inc
				);
			}
		}
	}
}