package com.danwink.strategymass.ai;

import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;

public class PlaceholderAI extends Bot
{
	boolean built = false;
	public void update( Player me, GameState state )
	{
		if( !built )
		{
			buildUnit();
			built = true;
		}
	}
}
