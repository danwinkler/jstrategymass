package com.danwink.strategymass.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.ClientUnit;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.game.objects.Team;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;

public class GameRenderer
{
	GameState state;
	SpriteBatch batch;
	
	Texture grass, tree;
	
	Texture p0, p1, mill;
	Texture millcolor;
	
	Texture b0, b1;
	Texture b0color;
	
	Texture m0, m1;
	
	Texture spear;
	
	public float millSpeed = 60;
	public float r = 0;
	
	Texture[] textureMap;
	
	public GameRenderer( GameState state )
	{
		this.state = state;
		
		this.batch = new SpriteBatch();
		
		grass = new Texture( Gdx.files.internal( "medievalTile_57.png" ) );
		tree = new Texture( Gdx.files.internal( "medievalTile_48.png" ) );
		
		p0 = new Texture( Gdx.files.internal( "medievalStructure_11.png" ) );
		p1 = new Texture( Gdx.files.internal( "medievalStructure_10.png" ) );
		mill = new Texture( Gdx.files.internal( "medievalStructure_13.png" ) );
		millcolor = new Texture( Gdx.files.internal( "medievalStructure_13.color.png" ) );
		
		b0 = new Texture( Gdx.files.internal( "medievalStructure_06.png" ) );
		b0color = new Texture( Gdx.files.internal( "medievalStructure_06.color.png" ) );
		b1 = new Texture( Gdx.files.internal( "medievalStructure_02.png" ) );
		
		m0 = new Texture( Gdx.files.internal( "medievalUnit_02.png" ) );
		m1 = new Texture( Gdx.files.internal( "medievalUnit_08.png" ) );
		
		spear = new Texture( Gdx.files.internal( "spear.png" ) );
		
		textureMap = new Texture[] { grass, tree, b0, p0 };
	}
	
	public void render( OrthographicCamera camera )
	{
		r += Gdx.graphics.getDeltaTime() * millSpeed;
		
		//Textures
		batch.setProjectionMatrix( camera.combined );
		batch.begin();
		if( state.map != null )
		{	
			renderMapBottom( batch );
			
			//Render Units
			for( UnitWrapper uw : state.units ) 
			{
				ClientUnit cu = (ClientUnit)uw;
				Unit u = cu.getUnit();
				batch.draw( cu.u.team == 0 ? m0 : m1, cu.x - 32, cu.y - 32 );
				//batch.draw( cu.u.team == 0 ? m0 : m1, u.pos.x - 32, u.pos.y - 32 );
			}
			
			for( Bullet b : state.bullets ) 
			{
				batch.draw( spear, b.pos.x - 16, b.pos.y - 16, 16, 16, 32, 32, 1, 1, MathUtils.radiansToDegrees * b.heading - 90, 0, 0, 32, 32, false, false );
			}
			
			renderMapTop( batch );
		}
		batch.end();
	}
	
	public void renderMapBottom( SpriteBatch batch )
	{
		//Render Grass
		for( int y = 0; y < state.map.height; y++ )
		{
			for( int x = 0; x < state.map.width; x++ )
			{
				int tId = state.map.tiles[y][x];
				batch.draw( tId == Map.TILE_TREE ? tree : grass, x*state.map.tileWidth, y*state.map.tileHeight );
			}
		}
		
		//Render bottom of buildings
		for( Point p : state.map.points )
		{
			if( p.isBase ) 
			{
				batch.draw( b0, p.pos.x - 32, p.pos.y - 48 );
			}
			else 
			{
				batch.draw( p0, p.pos.x - 32, p.pos.y - 48 );
			}
			
			if( p.team >= 0 && p.isBase )
			{
				batch.setColor( Team.colors[p.team] );
				batch.draw( b0color, p.pos.x - 32, p.pos.y - 48 );
				batch.setColor( Color.WHITE );
			}
		}
	}
	
	public void renderMapTop( SpriteBatch batch )
	{
		//Render top of buildings
		for( Point p : state.map.points )
		{
			if( p.isBase ) 
			{
				batch.draw( b1, p.pos.x - 32, p.pos.y + 16 );
			}
			else 
			{
				batch.draw( p1, p.pos.x - 32, p.pos.y + 16 );
				float rot = r;
				batch.draw( mill, p.pos.x - 32, p.pos.y - 16, 32, 32, 64, 64, 1, 1, rot, 0, 0, 64, 64, false, false );
				if( p.team >= 0 )
				{
					Color c = Team.colors[p.team];
					batch.setColor( c.r, c.g, c.b, p.taken*.01f );
					batch.draw( millcolor, p.pos.x - 32, p.pos.y - 16, 32, 32, 64, 64, 1, 1, rot, 0, 0, 64, 64, false, false );
					batch.setColor( Color.WHITE );
				}
			}
		}
	}

	public void dispose()
	{
		batch.dispose();
	}
}
