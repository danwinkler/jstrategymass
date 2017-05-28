package com.danwink.strategymass.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.net.SyncObject;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MapFileHelper
{
	static {
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
		return gson.fromJson( Gdx.files.local( "maps/test.json" ).readString(), Map.class );
	}
	
	public static void saveMap( Map m, String name )
	{
		FileHandle f = Gdx.files.local( "maps/" + name + ".json" );
		
		
		f.writeString( gson.toJson( m ), false );
	}
}
