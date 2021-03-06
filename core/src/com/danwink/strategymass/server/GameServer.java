package com.danwink.strategymass.server;

import java.io.IOException;
import java.util.ArrayList;

import com.danwink.dsync.DServer;
import com.danwink.dsync.ServerStateManager;
import com.danwink.strategymass.ai.Bot;
import com.danwink.strategymass.nethelpers.ClassRegister;

public class GameServer {
	public static final int TCP_PORT = 34124;
	public static final int UDP_PORT = 34125;

	public DServer server;
	ServerStateManager stateManager;

	public ArrayList<Bot> bots;

	boolean nextMap = false;

	public PlayState play = new PlayState();
	public LobbyState lobby = new LobbyState(this);
	public PostGameState postGame = new PostGameState();

	public GameServer() {
		server = new DServer();
		server.register(ClassRegister.classes);

		play.register(server);
		lobby.register(server);
		postGame.register(server);

		stateManager = new ServerStateManager(server);

		stateManager.add(ServerState.PLAY, play);
		stateManager.add(ServerState.LOBBY, lobby);
		stateManager.add(ServerState.POSTGAME, postGame);

		server.setState(ServerState.LOBBY);
	}

	public void start() {
		try {
			server.start(TCP_PORT, UDP_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		server.startThread(stateManager, 30);

		bots = new ArrayList<Bot>();
	}

	public void addBot(Bot b) {
		bots.add(b);
	}

	public void stop() {
		bots.forEach(bot -> bot.stop());
		server.stop();
	}
}
