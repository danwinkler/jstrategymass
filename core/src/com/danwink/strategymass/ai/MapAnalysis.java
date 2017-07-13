package com.danwink.strategymass.ai;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

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
import com.danwink.strategymass.game.objects.Bullet;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Point;

public class MapAnalysis
{
	int width, height;
	TileAnalysis[][] tiles; //xy instead of map's yx :/
	public Map m;
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
	
	public void spreadArea( int startX, int endX, int dx, int startY, int endY, int dy, TileAnalysis[][] changes, Field f ) throws IllegalArgumentException, IllegalAccessException
	{
		for( int x = startX; x != endX; x += dx )
		{
			for( int y = startY; y != endY; y += dy )
			{
				TileAnalysis t = tiles[x][y];
				
				if( f.get( t ) == null ) continue;
				
				spreadTile( t, x, y, x-1, y, changes, f );
				spreadTile( t, x, y, x+1, y, changes, f );
				spreadTile( t, x, y, x, y-1, changes, f );
				spreadTile( t, x, y, x, y+1, changes, f );
			}
		}
	}
	
	public void fillField( String fieldName ) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException
	{
		int midX = m.width / 2;
		int midY = m.height / 2;
		
		Field f = TileAnalysis.class.getField( fieldName );
		for( int i = 0; i < 500; i++ )
		{
			TileAnalysis[][] changes = new TileAnalysis[width][height];
			
			/*
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
			*/
			
			spreadArea( midX, -1, -1, midY, -1, -1, changes, f );
			spreadArea( midX+1, width, 1, midY, -1, -1, changes, f );
			spreadArea( midX, -1, -1, midY+1, height, 1, changes, f );
			spreadArea( midX+1, width, 1, midY+1, height, 1, changes, f );
			
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
			GridPoint2 t = m.worldToTile( b.pos.x, b.pos.y );
			if( b.isBase ) 
			{
				Zone z = new Zone();
				z.c = new Color( MathUtils.random(), MathUtils.random(), MathUtils.random(), .3f );
				z.p = b;
				z.adjacent = z.p.findAjacent( m );
				tiles[t.x][t.y].zone = z;
				zones.add( z );
				tiles[t.x][t.y].side = b.team;
			} 
			else
			{
				Zone z = new Zone();
				z.c = new Color( MathUtils.random(), MathUtils.random(), MathUtils.random(), .3f );
				z.p = b;
				z.adjacent = z.p.findAjacent( m );
				tiles[t.x][t.y].zone = z;
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
					ArrayList<GridPoint2> path = graph.search( bp.x, bp.y, zp.x, zp.y );
					if( path == null ) 
					{
						z.baseDistances[team] = 1000000;
					}
					else
					{
					z.baseDistances[team] = path.size();
					}
				}
				else 
				{
					z.baseDistances[team] = -1;
				}
			}
			z.bdString = Arrays.stream( z.baseDistances )
				.filter( i -> i != -1 )
				.mapToObj( i -> ((Integer) i).toString() )
				.collect( Collectors.joining( ", " ) );
		}
	}
	
	public void calculatePointVisibility()
	{
		for( Zone a : zones )
		{
			ArrayList<GridPoint2> at = a.p.adjacents( m );
			innerZone:
			for( Zone b : zones )
			{
				if( a == b ) continue;
				
				ArrayList<GridPoint2> bt = b.p.adjacents( m );
				
				for( GridPoint2 ag : at )
				{
					for( GridPoint2 bg : bt )
					{
						float ax = (ag.x+.5f) * m.tileWidth;
						float ay = (ag.y+.5f) * m.tileHeight;
						float bx = (bg.x+.5f) * m.tileWidth;
						float by = (bg.y+.5f) * m.tileHeight;
						
						if( !Bullet.hitwall( ax, ay, bx-ax, by-ay, m ) )
						{
							a.visible.add( b );
							continue innerZone;
						}
					}
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
		//pruneNeighbors( graph );
		
		//Calculate distances from bases
		calculateBaseDistances();
		
		//Calculate point visibility
		calculatePointVisibility();
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
		ArrayList<Zone> visible = new ArrayList<Zone>();
		Point p;
		GridPoint2 adjacent;
		int[] baseDistances = new int[4];
		String bdString;
		
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
		
		g.setColor( Color.CYAN );
		for( Zone z : zones )
		{
			for( Neighbor n : z.neighbors )
			{
				g.line( z.p.pos, n.z.p.pos );
			}
		}
		
		g.setColor( Color.YELLOW );
		for( Zone a : zones )
		{
			for( Zone b : a.visible )
			{
				g.line(
					a.p.pos.x + 5,
					a.p.pos.y + 5,
					b.p.pos.x + 5,
					b.p.pos.y + 5
				);
			}
		}
		g.end();
		
		BitmapFont f = new BitmapFont();
		batch.begin();
		for( Zone z : zones )
		{
			f.draw( batch, z.bdString, z.p.pos.x + 32, z.p.pos.y );
		}
		batch.end();
	}
	
	public Zone getZone( float x, float y )
	{
		GridPoint2 t = m.worldToTile( x, y );
		return tiles[t.x][t.y].zone;
	}
	
	public int getSide( float x, float y )
	{
		GridPoint2 t = m.worldToTile( x, y );
		return tiles[t.x][t.y].side;
	}
}
