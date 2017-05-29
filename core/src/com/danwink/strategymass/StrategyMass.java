package com.danwink.strategymass;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.danwink.strategymass.screens.play.Play;
import com.danwink.strategymass.server.GameServer;
import com.kotcrab.vis.ui.VisUI;

public class StrategyMass extends Game
{
	static StrategyMass game;
	
	public GameServer server;
	
	public void create()
	{
		VisUI.load();
		
		game = this;
		
		server = new GameServer();
		server.start();
		
		setScreen( new Play() );
		//setScreen( new MainMenu() );
	}
	
	public void render()
	{
		super.render();
	}
	
	public void dispose()
	{
		
	}
}
