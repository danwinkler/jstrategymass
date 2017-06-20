package com.danwink.strategymass;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class LoadScreen implements Screen
{
	AssetManager m;
	ShapeRenderer sr;
	
	String[] textures = new String[] {
		"grass_a",
		"tree_b",
		"unit_0",
		"unit_1",
		"base_bottom",
		"base_top",
		"base_bottom_color",
		"point_bottom",
		"point_top",
		"point_spinner",
		"point_spinner_color",
		"spear",
	};
	
	public void show()
	{
		m = Assets.m;
		
		for( String t : textures )
		{
			m.load( t + ".png", Texture.class );
		}
		
		sr = new ShapeRenderer();
	}

	public void render( float delta )
	{
		if( m.update() )
		{
			StrategyMass.game.setScreen( Screens.mainMenu );
		}
		
		float p = m.getProgress();
		
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
		
		sr.begin( ShapeType.Filled );
		sr.setColor( Color.BLUE );
		sr.rect( 50, Gdx.graphics.getHeight() * .5f - 20, (Gdx.graphics.getWidth() - 100) * p, 40 );
		sr.end();
	}

	public void resize( int width, int height )
	{
		
	}

	public void pause()
	{
		
	}

	public void resume()
	{
		
	}

	public void hide()
	{
		
	}

	public void dispose()
	{
		
	}
}
