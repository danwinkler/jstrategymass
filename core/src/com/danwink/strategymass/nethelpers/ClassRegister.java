package com.danwink.strategymass.nethelpers;

import java.util.ArrayList;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.MegaUnit;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.game.objects.RegularUnit;
import com.danwink.strategymass.game.objects.Team;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitPartial;
import com.danwink.strategymass.gamestats.GameStats;
import com.danwink.strategymass.gamestats.PlayerStats;
import com.danwink.strategymass.gamestats.TeamStats;
import com.danwink.dsync.sync.SyncObject;
import com.danwink.strategymass.nethelpers.Packets.MoveUnitPacket;
import com.danwink.strategymass.server.LobbyPlayer;
import com.danwink.strategymass.server.ServerState;

public class ClassRegister
{
	@SuppressWarnings( "rawtypes" )
	public static Class[] classes = {
		SyncObject.class,
		
		Map.class,
		Point.class,
		Unit.class,
		RegularUnit.class,
		MegaUnit.class,
		Team.class,
		Player.class,
		Bullet.class,
		
		ClientMessages.class,
		MoveUnitPacket.class,
		UnitPartial.class,
		
		ServerMessages.class,
		ServerState.class,
		LobbyPlayer.class,
		LobbyPlayer[].class,
		
		GameStats.class,
		PlayerStats.class,
		TeamStats.class,
		
		Vector2.class,
		GridPoint2.class,
		
		ArrayList.class,
		int[].class,
		int[][].class,
		String[].class,
	};
}
