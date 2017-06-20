package com.danwink.strategymass.screens.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.danwink.strategymass.MainMenu;
import com.danwink.strategymass.StrategyMass;
import com.danwink.strategymass.game.GameRenderer;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.MapFileHelper;
import com.danwink.strategymass.game.objects.Map;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.screens.editor.Brushes.TileBrush;
import com.danwink.strategymass.screens.editor.Brushes.BaseBrush;
import com.danwink.strategymass.screens.editor.Brushes.PointBrush;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

public class Editor implements Screen, InputProcessor
{
	OrthographicCamera camera;
	
	Stage stage;
	Table table;
	
	GameState state;
	GameRenderer r;
	
	SpriteBatch batch;
	
	InputMultiplexer input;
	
	float scrollSpeed = 300;
	float zoomSpeed = .1f;
	
	TextButton exit;
	TextButton grass;
	TextButton tree;
	TextButton base0, base1, base2, base3;
	TextButton point;
	
	VisSelectBox<Mirror> mirrorSelect;
	
	TextButton saveButton;
	TextButton loadButton;
	
	TextButton newButton;
	TextButton generateButton;
	
	Brush b = new TileBrush( Map.TILE_GRASS );
	
	public void show()
	{
		camera = new OrthographicCamera();
		camera.setToOrtho( false );
		camera.zoom = 2;
		
		input = new InputMultiplexer();
		
		stage = new Stage( new ScreenViewport());
		
		table = new Table();
		table.setFillParent( true );
		
		//table.setDebug( true );
		
		stage.addActor( table );
		
		input.addProcessor( stage );
		input.addProcessor( this );
		
		Gdx.input.setInputProcessor( input );
		
		exit = new VisTextButton( "Exit" );
		exit.addListener( new ClickListener() {
			public void clicked( InputEvent e, float x, float y ) 
			{
				StrategyMass.game.setScreen( new MainMenu() );
			}
		});
		
		grass = buildBrushButton( "Grass", new TileBrush( Map.TILE_GRASS ) );
		tree = buildBrushButton( "Tree", new TileBrush( Map.TILE_TREE ) );
		base0 = buildBrushButton( "Base 0", new BaseBrush( 0 ) );
		base1 = buildBrushButton( "Base 1", new BaseBrush( 1 ) );
		base2 = buildBrushButton( "Base 2", new BaseBrush( 2 ) );
		base3 = buildBrushButton( "Base 3", new BaseBrush( 3 ) );
		point = buildBrushButton( "Point", new PointBrush() );
		
		mirrorSelect = new VisSelectBox<Mirror>();
		mirrorSelect.setItems( 
			new Mirrors.None(), 
			new Mirrors.X(), 
			new Mirrors.Y(),
			new Mirrors.XY(),
			new Mirrors.FourWay()
		);
		
		saveButton = new VisTextButton( "Save" );
		saveButton.addListener( new ClickListener() {
			public void clicked( InputEvent e, float x, float y ) {
				String error = verifyMap();
				if( error != null ) 
				{
					Dialogs.showErrorDialog( stage, error );
					return;
				}
				
				VisTextField name = new VisTextField();
				name.setText( state.mapName );
				
				VisDialog d = new VisDialog( "Save Map" ) {
					public void result( Object obj )
					{
						if( (boolean)obj ) 
						{	
							String nameText = name.getText();
							if( nameText.isEmpty() ) return;
							MapFileHelper.saveMap( state.map, name.getText() );
						}
					}
				};
				
				Table t = d.getContentTable();
				t.add( "Name:" );
				t.add( name );
				
				d.button( "Save", true );
				d.button( "Cancel", false );
				
				d.show( stage );
			}
		});
		
		loadButton = new VisTextButton( "Load" );
		loadButton.addListener( new ClickListener() {
			public void clicked( InputEvent e, float x, float y ) {
				
				VisSelectBox<String> select = new VisSelectBox<String>();
				select.setItems( MapFileHelper.getMaps().toArray( new String[0] ) );
				
				VisDialog d = new VisDialog( "Load Map" ) {
					public void result( Object obj )
					{
						if( (boolean)obj ) 
						{
							state.mapName = select.getSelected();
							state.map = MapFileHelper.loadMap( select.getSelected() );
						}
					}
				};
				
				Table t = d.getContentTable();
				t.add( select );
				
				d.button( "Load", true );
				d.button( "Cancel", false );
				
				d.show( stage );
			}
		});
		
		newButton = new VisTextButton( "New" );
		newButton.addListener( new ClickListener() {
			public void clicked( InputEvent e, float x, float y ) {
				
				VisTextField wField = new VisTextField();
				VisTextField hField = new VisTextField();
				
				VisDialog d = new VisDialog( "New Map" ) {
					public void result( Object obj )
					{
						if( (boolean)obj ) 
						{
							try 
							{
								int w = Integer.parseInt( wField.getText() );
								int h = Integer.parseInt( hField.getText() );
								state.map = new Map( w, h );
							}
							catch( NumberFormatException ex )
							{
								
							}
						}
					}
				};
				
				Table t = d.getContentTable();
				t.add( "Width:" );
				t.add( wField );
				t.row();
				t.add( "Height:" );
				t.add( hField );
				
				d.button( "Create", true );
				d.button( "Cancel", false );
				
				d.show( stage );
			}
		});
		
		generateButton = new VisTextButton( "Generate" );
		generateButton.addListener( new ClickListener() {
			public void clicked( InputEvent e, float x, float y ) {
				MapGenerator.generate( state.map, mirrorSelect.getSelected() );
			}
		});
		
		
		table.add( grass ).fillX();
		table.row();
		table.add( tree ).fillX();
		table.row();
		table.add( base0 ).fillX();
		table.row();
		table.add( base1 ).fillX();
		table.row();
		table.add( base2 ).fillX();
		table.row();
		table.add( base3 ).fillX();
		table.row();
		table.add( point ).fillX().padBottom( 20 );
		table.row();
		
		table.add( mirrorSelect ).fillX().padBottom( 20 );
		table.row();
		
		
		table.add( saveButton ).fillX();
		table.row();
		table.add( loadButton ).fillX();
		table.row();
		table.add( newButton ).fillX();
		table.row();
		table.add( generateButton ).fillX();
		table.row();
		
		table.add( exit ).fillX().expandY().bottom();
		
		table.top().right();
		
		state = new GameState();
		state.map = new Map( 31, 31 );
		state.mapName = "";
		
		batch = new SpriteBatch();
		
		r = new GameRenderer( state );
	}
	
	public TextButton buildBrushButton( String text, Brush brush )
	{
		TextButton button = new VisTextButton( text );
		button.addListener( new ClickListener() {
			public void clicked( InputEvent e, float x, float y ) 
			{
				b = brush;
			}
		});
		return button;
	}
	
	public void draw( int x, int y )
	{
		if( x < 0 || x >= state.map.width || y < 0 || y >= state.map.height ) return;
		
		if( b.mirrorable )
		{
			mirrorSelect.getSelected().draw( x, y, b, state.map );
		}
		else 
		{
			b.draw( x, y, state.map );
		}
	}
	
	public String verifyMap()
	{
		Map m = state.map;
		
		//Check that bases are sequential and that there are enough bases
		int lastBase = 0;
		int numBases = 0;
		for( int i = 0; i < 4; i++ )
		{
			Point p = m.getBase( i );
			if( p != null )
			{
				if( i - lastBase >= 2 ) 
				{
					return "Bases must be sequential"; //TODO: better error message
				}
				lastBase = i;
				numBases++;
			}
		}
		
		if( numBases < 2 ) 
		{
			return "Must have at least 2 bases";
		}
		
		m.teams = numBases;
		
		return null;
	}

	public void render( float dt )
	{
		if( Gdx.input.isKeyPressed( Input.Keys.LEFT ) )
		{
			camera.translate( -scrollSpeed * dt * camera.zoom, 0 );
			camera.update();
		}
		if( Gdx.input.isKeyPressed( Input.Keys.RIGHT ) )
		{
			camera.translate( scrollSpeed * dt * camera.zoom, 0 );
			camera.update();
		}
		if( Gdx.input.isKeyPressed( Input.Keys.DOWN ) )
		{
			camera.translate( 0, -scrollSpeed * dt * camera.zoom );
			camera.update();
		}
		if( Gdx.input.isKeyPressed( Input.Keys.UP ) )
		{
			camera.translate( 0, scrollSpeed * dt * camera.zoom );
			camera.update();
		}
		
		r.r += Gdx.graphics.getDeltaTime() * r.millSpeed;
		
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
		
		batch.setProjectionMatrix( camera.combined );
		batch.begin();
		
		r.renderMapBottom( batch );
		r.renderMapTop( batch );
		
		batch.end();
		
		stage.act( Gdx.graphics.getDeltaTime() );
		stage.draw();
	}

	public void resize( int width, int height )
	{
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		
		camera.update();
		
		stage.getViewport().update( width, height, true );
	}

	public void pause()
	{
		
	}

	public void resume()
	{
		
	}

	public void hide()
	{
		
	}

	public void dispose()
	{
		stage.dispose();
	}

	public boolean keyDown( int keycode )
	{
		return false;
	}

	public boolean keyUp( int keycode )
	{
		return false;
	}

	public boolean keyTyped( char character )
	{
		return false;
	}

	public boolean touchDown( int screenX, int screenY, int pointer, int button )
	{
		Vector3 mousePosScreen = new Vector3( Gdx.input.getX(), Gdx.input.getY(), 0 );
		Vector3 world = camera.unproject( mousePosScreen );
		
		int x = (int)(world.x / state.map.tileWidth);
		int y = (int)(world.y / state.map.tileHeight);
		
		draw( x, y );
		
		return true;
	}

	public boolean touchUp( int screenX, int screenY, int pointer, int button )
	{
		return false;
	}

	public boolean touchDragged( int screenX, int screenY, int pointer )
	{
		Vector3 mousePosScreen = new Vector3( Gdx.input.getX(), Gdx.input.getY(), 0 );
		Vector3 world = camera.unproject( mousePosScreen );
		
		int x = (int)(world.x / state.map.tileWidth);
		int y = (int)(world.y / state.map.tileHeight);
		
		draw( x, y );
		
		return true;
	}

	public boolean mouseMoved( int screenX, int screenY )
	{
		return false;
	}

	public boolean scrolled( int amount )
	{
		Vector3 mousePosScreen = new Vector3( Gdx.input.getX(), Gdx.input.getY(), 0 );
		Vector3 worldA = camera.unproject( mousePosScreen.cpy() );
		
		camera.zoom += camera.zoom * amount * zoomSpeed;
		camera.update();
		
		Vector3 worldB = camera.unproject( mousePosScreen );
		
		if( amount < 0 )
		camera.translate( worldA.x - worldB.x, worldA.y - worldB.y );
		
		camera.update();
		
		return true;
	}	
}
