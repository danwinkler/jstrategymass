package com.danwink.strategymass.ai;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.danwink.strategymass.game.MapPathFinding;
import com.danwink.strategymass.game.MapPathFinding.MapGraph;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Point;

public class MapAnalysis
{
	int width, height;
	TileAnalysis[][] tiles; //xy instead of map's yx :/
	Map m;
	ArrayList<Zone> zones = new ArrayList<Zone>();
	MapGraph graph;
	
	public void spreadTile( TileAnalysis ta, int x, int y, int tx, int ty, TileAnalysis[][] changes, Field f ) throws IllegalArgumentException, IllegalAccessException
	{
		if( tx >= width || tx < 0 || ty >= height || ty < 0 ) return;
		if( m.isPassable( tx, ty ) && f.get( tiles[tx][ty] ) == null ) 
		{
			TileAnalysis nt = new TileAnalysis( tiles[tx][ty] );
			f.set( nt, f.get( ta ) );
			changes[tx][ty] = nt;
		}
	}
	
	public void fillField( String fieldName ) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException
	{
		Field f = TileAnalysis.class.getField( fieldName );
		for( int i = 0; i < 500; i++ )
		{
			TileAnalysis[][] changes = new TileAnalysis[width][height];
			for( int x = 0; x < width; x++ )
			{
				for( int y = 0; y < height; y++ )
				{
					TileAnalysis t = tiles[x][y];
					
					if( f.get( t ) == null ) continue;
					
					spreadTile( t, x, y, x-1, y, changes, f );
					spreadTile( t, x, y, x+1, y, changes, f );
					spreadTile( t, x, y, x, y-1, changes, f );
					spreadTile( t, x, y, x, y+1, changes, f );
				}
			}
			
			boolean canExit = true;
			for( int x = 0; x < width; x++ )
			{
				for( int y = 0; y < height; y++ )
				{
					if( changes[x][y] != null ) 
					{
						tiles[x][y] = changes[x][y];
						canExit = false;
					}
				}
			}
			if( canExit )
			{
				break;
			}
		}
	}
	
	public void setAdjacent( int x, int y, int tx, int ty )
	{
		if( tx < 0 || tx >= width || ty < 0 || ty >= height ) return;
		TileAnalysis t = tiles[x][y];
		TileAnalysis a = tiles[tx][ty];
		if( t.zone != null && a.zone != null && t.zone != a.zone )
		{
			t.zone.addNeightbor( a.zone );
		}
	}
	
	public void createInitialZones()
	{
		for( Point b : m.points )
		{
			if( b.isBase ) 
			{
				int x = (int)(b.pos.x / m.tileWidth);
				int y = (int)(b.pos.y / m.tileHeight);
				Zone z = new Zone();
				z.c = new Color( MathUtils.random(), MathUtils.random(), MathUtils.random(), .3f );
				z.p = b;
				z.adjacent = z.p.findAjacent( m );
				tiles[x][y].zone = z;
				zones.add( z );
				tiles[x][y].side = b.team;
			} 
			else
			{
				int x = (int)(b.pos.x/m.tileWidth);
				int y = (int)(b.pos.y/m.tileHeight);
				Zone z = new Zone();
				z.c = new Color( MathUtils.random(), MathUtils.random(), MathUtils.random(), .3f );
				z.p = b;
				z.adjacent = z.p.findAjacent( m );
				tiles[x][y].zone = z;
				zones.add( z );
			}
		}
	}
	
	public void pruneNeighbors( MapGraph graph )
	{
		for( Zone z : zones )
		{
			for( int i = 0; i < z.neighbors.size(); i++ )
			{
				Neighbor n = z.neighbors.get( i );
				
				ArrayList<GridPoint2> path = graph.search( z.adjacent.x, z.adjacent.y, n.z.adjacent.x, n.z.adjacent.y );
				n.distance = path.size();
				
				for( int j = 0; j < path.size(); j++ )
				{
					GridPoint2 s = path.get( j );
					TileAnalysis ta = tiles[s.x][s.y];
					if( ta.zone != z && ta.zone != n.z )
					{
						float dx = ta.zone.p.pos.x - (s.x*m.tileWidth);
						float dy = ta.zone.p.pos.y - (s.x*m.tileHeight);
						float distance = (float)Math.sqrt( (dx*dx) + (dy*dy) );
						
						if( distance < (5*m.tileWidth) && z.neighbors.size() >= 2 )
						{
							z.neighbors.remove( i );
							i--;
							break;
						}
					}
				}
			}
		}
	}
	
	public void calculateBaseDistances()
	{
		for( Zone z : zones )
		{
			GridPoint2 zp = z.p.findAjacent( m );
			for( int team = 0; team < 4; team++ )
			{
				Point base = m.getBase( team );
				if( base != null )
				{
					GridPoint2 bp = base.findAjacent( m );
					z.baseDistances[team] = graph.search( bp.x, bp.y, zp.x, zp.y ).size();
				}
			}
		}
	}
	
	public void build( Map m )
	{
		this.m = m;
		width = m.width;
		height = m.height;
		graph = new MapPathFinding.MapGraph( m );
		
		tiles = new TileAnalysis[width][height];
		for( int x = 0; x < width; x++ )
		{
			for( int y = 0; y < height; y++ )
			{
				tiles[x][y] = new TileAnalysis();
			}
		}
		
		//Set initial zones
		createInitialZones();
		
		//Flood fill
		try
		{
			fillField( "zone" );
			fillField( "side" );
		}
		catch( IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//for each zone, calculate adjacent zones, and distances to each zone
		for( int x = 0; x < width; x++ )
		{
			for( int y = 0; y < height; y++ )
			{
				setAdjacent( x, y, x-1, y );
				setAdjacent( x, y, x+1, y );
				setAdjacent( x, y, x, y-1 );
				setAdjacent( x, y, x, y+1 );
			}
		}
		
		//Calculate zone distances
		//Prune neighbors whose paths force us to go through another zone's building (so we dont feed)
		pruneNeighbors( graph );
		
		//Calculate distances from bases
		calculateBaseDistances();
	}
	
	class TileAnalysis
	{
		public int side;
		public Zone zone;
		
		public TileAnalysis()
		{
			
		}
		
		public TileAnalysis( TileAnalysis ta )
		{
			this.side = ta.side;
			this.zone = ta.zone;
		}
	}
	
	class Zone
	{
		Color c;
		ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();
		Point p;
		GridPoint2 adjacent;
		int[] baseDistances = new int[4];
		
		public void addNeightbor( Zone z )
		{
			for( Neighbor n : neighbors )
			{
				if( n.z == z ) return;
			}
			neighbors.add( new Neighbor( z ) );
		}
	}
	
	class Neighbor
	{
		Zone z;
		int distance;
		
		public Neighbor( Zone z )
		{
			this.z = z;
		}
	}
	
	public void render( ShapeRenderer g, SpriteBatch batch )
	{
		Gdx.gl.glEnable( GL30.GL_BLEND );
		Gdx.gl.glBlendFunc( GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA );
		g.begin( ShapeType.Filled );
		for( int x = 0; x < width; x++ )
		{
			for( int y = 0; y < height; y++ )
			{
				TileAnalysis ta = tiles[x][y];
				if( ta.zone != null ) 
				{
					g.setColor( ta.zone.c );
					g.rect( x * m.tileWidth, y * m.tileHeight, m.tileWidth, m.tileHeight );
				}
				if( ta.side >= 0 )
				{
					//g.setColor( new Color( ta.side.getColor().r, ta.side.getColor().g, ta.side.getColor().b, .1f ) );
					//g.fillRect( x * Level.tileSize, y * Level.tileSize, Level.tileSize, Level.tileSize );
					//g.setColor( Color.black );
					//g.drawString( Integer.toString( ta.side.id ), x * Level.tileSize + 5, y * Level.tileSize );
				}
			}
		}
		g.end();
		Gdx.gl.glDisable( GL30.GL_BLEND );
		
		g.begin( ShapeType.Line );
		for( Zone z : zones )
		{
			for( Neighbor n : z.neighbors )
			{
				g.line( z.p.pos, n.z.p.pos );
			}
		}
		g.end();
		
		BitmapFont f = new BitmapFont();
		batch.begin();
		for( Zone z : zones )
		{
			f.draw( batch, z.baseDistances[0] + ", " + z.baseDistances[1], z.p.pos.x + 32, z.p.pos.y );
		}
		batch.end();
	}
	
	public Zone getZone( float x, float y )
	{
		return tiles[(int)(x/m.tileWidth)][(int)(y/m.tileHeight)].zone;
	}
	
	public int getSide( float x, float y )
	{
		return tiles[(int)(x/m.tileWidth)][(int)(y/m.tileHeight)].side;
	}
}
