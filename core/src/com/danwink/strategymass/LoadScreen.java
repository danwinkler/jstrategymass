package com.danwink.strategymass;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class LoadScreen implements Screen
{
	AssetManager m;
	ShapeRenderer sr;
	BitmapFont f;
	SpriteBatch batch;
	
	String[] textures = new String[] {
		"grass_a",
		"tree_b",
		"unit_0",
		"unit_1",
		"unit_2",
		"unit_3",
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
		f = new BitmapFont();
		batch = new SpriteBatch();
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
		
		batch.begin();
		f.draw( batch, (int)(p*100) + "%", Gdx.graphics.getWidth() * .5f, Gdx.graphics.getHeight() * .5f );
		batch.end();
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
