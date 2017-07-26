package com.danwink.strategymass.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.danwink.strategymass.Assets;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.ai.MapAnalysis;
import com.danwink.strategymass.ai.Tiberius;
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.ClientUnit;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.MegaUnit;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.game.objects.RegularUnit;
import com.danwink.strategymass.game.objects.Team;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;

public class GameRenderer
{
	GameState state;
	SpriteBatch batch;
	ShapeRenderer shape;
	
	public Texture grass, tree;
	
	Texture p0, p1, mill;
	Texture millcolor;
	
	Texture b0, b1;
	Texture b0color;
	
	Texture u0, u1, u2, u3;
	Texture m0, m1, m2, m3;
	Texture s0, s1, s2, s3;
	Texture myunit;
	
	Texture spear;
	
	public float millSpeed = 60;
	public float r = 0;
	
	Texture[] textureMap;
	Texture[] unitMap;
	Texture[] manMap;
	Texture[] shieldMap;
	
	MapAnalysis ma;
	boolean debug;
	
	public GameRenderer( GameState state )
	{
		this.state = state;
		
		this.batch = new SpriteBatch();
		shape = new ShapeRenderer();
		
		grass = Assets.getT( "grass_a" );
		tree = Assets.getT( "tree_b" );
		
		p0 = Assets.getT( "point_bottom" );
		p1 = Assets.getT( "point_top" );
		mill = Assets.getT( "point_spinner" );
		millcolor = Assets.getT( "point_spinner_color" );
		
		b0 = Assets.getT( "base_bottom" );
		b0color = Assets.getT( "base_bottom_color" );
		b1 = Assets.getT( "base_top" );
		
		u0 = Assets.getT( "unit_0" );
		u1 = Assets.getT( "unit_1" );
		u2 = Assets.getT( "unit_2" );
		u3 = Assets.getT( "unit_3" );
		
		m0 = Assets.getT( "man_0" );
		m1 = Assets.getT( "man_1" );
		m2 = Assets.getT( "man_2" );
		m3 = Assets.getT( "man_3" );

		s0 = Assets.getT( "shield_0" );
		s1 = Assets.getT( "shield_1" );
		s2 = Assets.getT( "shield_2" );
		s3 = Assets.getT( "shield_3" );

		
		myunit = Assets.getT( "unit_necklace" );
		
		spear = Assets.getT( "spear" );
		
		unitMap = new Texture[] { u0, u1, u2, u3 };
		manMap = new Texture[] { m0, m1, m2, m3 };
		shieldMap = new Texture[] { s0, s1, s2, s3 };
		textureMap = new Texture[] { grass, tree, b0, p0 };
	}
	
	public void render( OrthographicCamera camera, Player owner )
	{
		r += Gdx.graphics.getDeltaTime() * millSpeed;
		
		//Textures
		if( state.map != null )
		{	
			if( ma == null || ma.m != state.map )
			{
				ma = new MapAnalysis();
				ma.build( state.map );
			}
			
			batch.setProjectionMatrix( camera.combined );
			batch.begin();
			
			renderMapBottom( batch );
			
			//Render Units
			for( UnitWrapper uw : state.units ) 
			{
				ClientUnit cu = (ClientUnit)uw;
				Unit u = cu.getUnit();
				if( u instanceof RegularUnit )
				{
					float size = 64;
					float h_size = size*.5f;
					batch.draw( unitMap[cu.u.team], cu.x - h_size, cu.y - h_size, size, size );
					if( owner.playerId == cu.u.owner )
					{
						batch.draw( myunit, cu.x - h_size, cu.y - h_size, size, size );
					}
				}
				else
				{
					float size = 128;
					float h_size = size*.5f;
					batch.draw( shieldMap[cu.u.team], cu.x - h_size, cu.y - h_size, size, size );
					if( owner.playerId == cu.u.owner )
					{
						batch.draw( myunit, cu.x - h_size, cu.y - h_size, size, size );
					}
				}
			}
			
			for( Bullet b : state.bullets ) 
			{
				float scale = b.dieOnHit ? 1 : 1.6f;
				batch.draw( spear, b.pos.x - 16, b.pos.y - 16, 16, 16, 32, 32, scale, scale, MathUtils.radiansToDegrees * b.heading - 90, 0, 0, 32, 32, false, false );
			}
			
			renderMapTop( batch );
			batch.end();
			
			if( debug )
			{
				shape.setProjectionMatrix( camera.combined );
				ma.render( shape, batch );
			}
		}
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
	
	public void toggleDebug()
	{
		debug = !debug;
	}
}
