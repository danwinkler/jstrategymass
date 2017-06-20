package com.danwink.strategymass;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

public class Assets
{
	public static AssetManager m = new AssetManager();

	public static Texture getT( String string )
	{
		return m.get( string + ".png", Texture.class );
	}
}
