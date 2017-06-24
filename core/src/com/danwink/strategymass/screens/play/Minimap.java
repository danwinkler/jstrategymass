package com.danwink.strategymass.screens.play;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.danwink.strategymass.game.GameRenderer;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Map;

public class Minimap extends Actor
{
	OrthographicCamera camera;
	GameState state;
	GameRenderer r;
	
	public Minimap()
	{
		super();
		this.setSize( 200, 200 );
		
		state = new GameState();
		
		r = new GameRenderer( state );
		
		camera = new OrthographicCamera();
	}
	
	public void setMap( Map m )
	{
		state.map = m;
		computeSize();
	}
	
	private void computeSize()
	{
		Map m = state.map;
		
		float xo, yo, inc;
		
		if( m.width >= m.height )
		{
			inc = getWidth() / m.width;
			xo = 0;
			yo = (getHeight() - (inc * m.height)) * .5f;
		}
		else
		{
			inc = getHeight() / m.height;
			xo = (getWidth() - (inc * m.width)) * .5f;
			yo = 0;
		}
		
		int mx = m.width * m.tileWidth;
		int my = m.height * m.tileHeight;
		float rx = mx / getWidth();
		float ry = my / getHeight();
		camera.setToOrtho( false );
		camera.zoom = rx < ry ? ry : rx;
		camera.position.set( ((Gdx.graphics.getWidth() * .5f) - getX() - xo) * camera.zoom, ((Gdx.graphics.getHeight() * .5f) - getY() - yo) * camera.zoom, 0 );
		
		camera.update();
	}
	
	public void draw( Batch batch, float parentActor )
	{
		SpriteBatch b = (SpriteBatch)batch;
		
		Map m = state.map;
		
		if( m == null ) return;
		
		computeSize();
		
		Matrix4 prev = b.getProjectionMatrix().cpy();
		
		b.setProjectionMatrix( camera.combined );
		
		r.r += Gdx.graphics.getDeltaTime() * r.millSpeed;
		r.renderMapBottom( b );
		r.renderMapTop( b );
		
		b.setProjectionMatrix( prev );
	}
}