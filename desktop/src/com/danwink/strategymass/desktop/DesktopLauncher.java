package com.danwink.strategymass.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.danwink.strategymass.StrategyMass;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1200;
		config.height = 800;
		config.samples = 4; 
		new LwjglApplication(new StrategyMass(), config);
	}
}
