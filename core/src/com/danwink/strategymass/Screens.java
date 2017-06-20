package com.danwink.strategymass;

import com.danwink.strategymass.screens.play.ConnectedScreen;
import com.danwink.strategymass.screens.play.ConnectingScreen;
import com.danwink.strategymass.screens.play.LobbyScreen;
import com.danwink.strategymass.screens.play.PlayScreen;

public class Screens
{
	public static LoadScreen load = new LoadScreen();
	public static MainMenu mainMenu = new MainMenu();
	
	//Play Screens
	public static ConnectedScreen connected = new ConnectedScreen();
	public static ConnectingScreen connecting = new ConnectingScreen();
	public static LobbyScreen lobby = new LobbyScreen();
	public static PlayScreen play = new PlayScreen();
}
