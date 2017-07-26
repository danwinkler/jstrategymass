package com.danwink.strategymass;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.danwink.strategymass.AudioManager.GameSound;

public class LoadScreen implements Screen
{
	AssetManager m;
	ShapeRenderer sr;
	BitmapFont f;
	SpriteBatch batch;
	
	//TODO: this seems like a poor place for these lists
	String[] textures = {
		"grass_a",
		"tree_b",
		"unit_0",
		"unit_1",
		"unit_2",
		"unit_3",
		"man_0",
		"man_1",
		"man_2",
		"man_3",
		"shield_0",
		"shield_1",
		"shield_2",
		"shield_3",
		"unit_necklace",
		"base_bottom",
		"base_top",
		"base_bottom_color",
		"point_bottom",
		"point_top",
		"point_spinner",
		"point_spinner_color",
		"spear",
	};
	
	String[] fonts = {
		"title.fnt"
	};
	
	public void show()
	{
		m = Assets.m;
		
		TextureParameter param = new TextureParameter();
		param.minFilter = TextureFilter.Linear;
		param.genMipMaps = true;
		
		for( String t : textures )
		{
			m.load( "image/" + t + ".png", Texture.class, param );
		}
		
		for( GameSound s : AudioManager.GameSound.values() )
		{
			m.load( s.path, Sound.class );
		}
		
		for( String f : fonts )
		{
			m.load( "font/" + f, BitmapFont.class );
		}
		
		sr = new ShapeRenderer();
		f = new BitmapFont();
		batch = new SpriteBatch();
	}

	public void render( float delta )
	{
		if( m.update() )
		{
			AudioManager.initSounds();
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
