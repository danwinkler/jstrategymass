package com.danwink.strategymass.game;

import java.util.ArrayList;
import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.dsync.sync.SyncObject;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MapFileHelper
{
	final static String localMapsPath = "maps/";
	final static String devMapsPath = "desktop/maps/";
	static String mapsPath;

	static {
		mapsPath = Gdx.files.local(devMapsPath).exists() ? devMapsPath : localMapsPath;

		GsonBuilder builder = new GsonBuilder();
		builder.setExclusionStrategies( new ExclusionStrategy() {
			public boolean shouldSkipField( FieldAttributes f )
			{
				if( f.getDeclaringClass() == SyncObject.class ) return true;
				return false;
			}

			public boolean shouldSkipClass( Class<?> clazz )
			{
				return false;
			}
		});
		builder.serializeNulls();
		
		gson = builder.create();
	}
	
	static Gson gson;
	
	public static Map loadMap( String name )
	{
		Map m = gson.fromJson( Gdx.files.local( mapsPath + name + ".json" ).readString(), Map.class );
		
		//Support for legacy maps
		if( m.teams == 0 )
		{
			m.teams = 2;
		}
		
		return m;
	}
	
	public static void saveMap( Map m, String name )
	{
		FileHandle f = Gdx.files.local( mapsPath + name + ".json" );
		
		f.writeString( gson.toJson( m ), false );
	}
	
	public static ArrayList<String> getMaps()
	{
		ArrayList<String> maps = new ArrayList<String>();
		Arrays.asList( Gdx.files.local(mapsPath).list() ).forEach( f -> {
			if( f.extension().equals( "json" ) ) {
				maps.add( f.nameWithoutExtension() );
			}
		});
		return maps;
	}
}
