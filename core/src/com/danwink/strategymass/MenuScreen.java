package com.danwink.strategymass;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class MenuScreen implements Screen
{
	public Stage stage;
	public Table table;
	
	public void show()
	{
		stage = new Stage( new ScreenViewport());
		
		table = new Table();
		table.setFillParent( true );
		stage.addActor( table );
		
		//table.setDebug( true );
		
		Gdx.input.setInputProcessor( stage );
		build();
	}
	
	public abstract void build();

	public void render( float delta )
	{
		stage.act( Gdx.graphics.getDeltaTime() );
		
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
		stage.draw();
	}

	public void resize( int width, int height )
	{
		stage.getViewport().update( width, height, true );
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
		//stage.dispose();
	}
}
