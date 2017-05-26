package com.danwink.strategymass;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class StrategyMass extends Game
{
	GameServer server;
	
	SpriteBatch batch;
	
	public void create()
	{
		batch = new SpriteBatch();

		server = new GameServer();
		server.start();
		
		setScreen( new Play( this ) );
	}
	
	public void render()
	{
		super.render();
	}
	
	public void dispose()
	{
		batch.dispose();
	}
}
