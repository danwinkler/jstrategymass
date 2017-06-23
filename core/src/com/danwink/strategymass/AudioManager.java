package com.danwink.strategymass;

import java.util.HashMap;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class AudioManager
{
	static OrthographicCamera camera;
	static HashMap<GameSound, Sound> soundMap = new HashMap<>();
	
	public static void setCamera( OrthographicCamera camera )
	{
		AudioManager.camera = camera;
	}

	public static void initSounds()
	{
		for( GameSound s : GameSound.values() )
		{
			soundMap.put( s, Assets.getS( s.path ) );
		}
	}

	public static void play( GameSound sound, Vector2 pos )
	{
		soundMap.get( sound ).play( MathUtils.clamp( 1.f / camera.zoom, .1f, 1 ), MathUtils.random( .8f, 1.1f ), 0 );
	}
	
	public static enum GameSound
	{
		THROW_SPEAR( "throw.wav" ),
		UNIT_HIT( "hit.wav" );
		
		String path;
		
		GameSound( String file )
		{
			this.path = "sound/" + file;
		}
	}
}
