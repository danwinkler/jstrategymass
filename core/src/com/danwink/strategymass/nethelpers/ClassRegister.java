package com.danwink.strategymass.nethelpers;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.gameobjects.Point;
import com.danwink.strategymass.gameobjects.Map;
import com.danwink.strategymass.gameobjects.Team;
import com.danwink.strategymass.gameobjects.Unit;
import com.danwink.strategymass.net.SyncObject;

public class ClassRegister
{
	public static Class[] classes = {
		SyncObject.class,
		
		Map.class,
		Point.class,
		Unit.class,
		Team.class,
		
		ClientMessages.class,
		ServerMessages.class,
		
		Vector2.class,
		
		ArrayList.class,
		int[].class,
		int[][].class
	};
}
