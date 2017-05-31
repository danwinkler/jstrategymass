package com.danwink.strategymass.ai;

import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;

public class PlaceholderAI extends Bot
{
	public void update( Player me, GameState state )
	{
		if( Math.random() < .1 )
			buildUnit();
	}
}
