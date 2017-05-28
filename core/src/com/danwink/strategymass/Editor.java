package com.danwink.strategymass;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.danwink.strategymass.game.GameRenderer;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Map;

public class Editor implements Screen
{
	Stage stage;
	Table table;
	
	GameState state;
	GameRenderer r;
	
	public void show()
	{
		stage = new Stage( new ScreenViewport());
		
		table = new Table();
		table.setFillParent( true );
		stage.addActor( table );
		
		Gdx.input.setInputProcessor( stage );
		
		state = new GameState();
		state.map = new Map( 31, 31 );
	}

	public void render( float delta )
	{
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
		
		
		
		stage.act( Gdx.graphics.getDeltaTime() );
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
		stage.dispose();
	}	
}
