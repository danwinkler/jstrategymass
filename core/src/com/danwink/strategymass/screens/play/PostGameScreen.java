package com.danwink.strategymass.screens.play;

import java.util.ArrayList;
import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.danwink.dsync.DClient;
import com.danwink.libgdx.form.FormClient;
import com.danwink.strategymass.MenuScreen;
import com.danwink.strategymass.Screens;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.gamestats.GameStats;
import com.danwink.strategymass.gamestats.TeamStats;
import com.danwink.strategymass.nethelpers.ServerMessages;
import com.danwink.strategymass.server.ServerState;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPaneAdapter;

public class PostGameScreen extends MenuScreen
{
	DClient client;
	
	Graph units;
	Graph points;
	
	FormClient fc;
	
	public void build()
	{
		units = new Graph();
		points = new Graph();
		
		TabbedPane tp = new TabbedPane();
		Table contentTable = new Table();
		
		table.add( tp.getTable() ).width( 600 );
		table.row();
		table.add( contentTable ).width( 600 ).height( 400 );
		table.row();
		
		VisTextButton next = new VisTextButton( "Continue" );
		
		table.add( next );
		
		fc.add( "next", next );
		
		tp.addListener( new TabbedPaneAdapter() {
			public void switchedTab( Tab tab )
			{
				contentTable.clearChildren();
				contentTable.add( tab.getContentTable() ).expand().fill();
			}
		});
		
		tp.add( new GraphPane( "Units", units ) );
		tp.add( new GraphPane( "Points", points ) );	
		
		tp.switchTab( 0 );
	}
	
	public void register( DClient client )
	{
		this.client = client;
		
		fc = new FormClient( client, ServerState.POSTGAME );
		
		client.on( ServerState.POSTGAME, ServerMessages.POSTGAME_STATS, (GameStats stats) -> {
			for( TeamStats ts : stats.teamStats )
			{
				units.addLine( ts.units, ts.t.getColor() );
				points.addLine( ts.points, ts.t.getColor() );
			}
		});
		
		client.on( ServerState.POSTGAME, DClient.DISCONNECTED, o -> {
			StrategyMass.game.setScreen( Screens.mainMenu );
		});
	}
	
	@Override
	public void render( float dt )
	{
		super.render( dt );
	}
	
	public static class GraphPane extends Tab
	{
		Table content = new Table();
		private String name;
		private Graph g;

		public GraphPane( String name, Graph g )
		{
			super( false, false );
			this.name = name;
			this.g = g;
			
			content.add( g ).fill().expand();
		}
		
		public String getTabTitle()
		{
			return name;
		}

		@Override
		public Table getContentTable()
		{
			return content;
		}
	}
	
	public static class Graph extends Actor
	{
		ShapeRenderer sr = new ShapeRenderer();
		ArrayList<ArrayList<Integer>> lines = new ArrayList<>();
		ArrayList<Color> colors = new ArrayList<Color>();
		float maxNum, maxMag;
		
		public void addLine( ArrayList<Integer> line )
		{
			addLine( line, Color.BLUE );
		}
		
		public void addLine( ArrayList<Integer> line, Color c )
		{
			lines.add( line );
			colors.add( c );
			maxNum = Math.max( maxNum, line.size() );
			maxMag = Math.max( maxMag, line.stream().max( Comparator.<Integer>naturalOrder() ).get() );
		}
		
		@Override
		public void draw( Batch batch, float parentActor )
		{
			batch.end();
			
			sr.setProjectionMatrix( batch.getProjectionMatrix() );
			sr.setTransformMatrix( batch.getTransformMatrix() );
			sr.translate( getX(), getY(), 0 );
			
			sr.begin( ShapeType.Filled );
			float dx = this.getWidth() / (maxNum-1);
			float dy = (this.getHeight()-10) / maxMag;
			
			sr.translate( 0, 5, 0 );
			int closestPointL = -1;
			int closestPointI = -1;
			float closestPointD = 20;
			Vector2 m = new Vector2( Gdx.input.getX() - getX(), (Gdx.graphics.getHeight() - Gdx.input.getY()) - getY() );
			for( int l = 0; l < lines.size(); l++ )
			{
				ArrayList<Integer> line = lines.get( l );
				Color c = colors.get( l );
				for( int i = 0; i < line.size()-1; i++ )
				{
					sr.setColor( c );
					sr.rectLine(
						i * dx,
						line.get( i ) * dy,
						(i+1) * dx,
						line.get( i+1 ) * dy,
						2
					);
					
					float d = m.dst( i * dx, line.get( i ) * dy );
					if( d < closestPointD )
					{
						closestPointL = l;
						closestPointI = i;
						closestPointD = d;
					}
				}
			}
			
			
			if( closestPointL >= 0 )
			{
				sr.setColor( colors.get( closestPointL ) );
				sr.circle( closestPointI * dx, lines.get( closestPointL ).get( closestPointI ) * dy, 5 );
			}
			
			sr.end();
			
			batch.begin();
			
			if( closestPointL >= 0 )
			{
				BitmapFont f = new BitmapFont();
				f.draw( 
					batch, 
					lines.get( closestPointL ).get( closestPointI ) + "", 
					getX() + closestPointI * dx + 5, 
					getY() + lines.get( closestPointL ).get( closestPointI ) * dy + 15 
				);
			}
		}
	}
}
