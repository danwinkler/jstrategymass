package com.danwink.strategymass;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class Assets
{
	public static AssetManager m = new AssetManager();

	public static Texture getT( String string )
	{
		return m.get( "image/" + string + ".png", Texture.class );
	}

	public static Sound getS( String string )
	{
		return m.get( string, Sound.class );
	}
}
