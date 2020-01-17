package com.danwink.strategymass;

import java.util.Arrays;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

public class SettingsScreen extends MenuScreen
{
	Setting[] settings = {
		new Setting<String>( "Name", "name", SettingType.STRING, "Player" ),
		new Setting<String>( "Max Units", "maxunits", SettingType.INT, 1500 ),
	};

	public void build()
	{
		Preferences prefs = StrategyMass.getSettings();
		
		VisTextButton back = new VisTextButton( "Back" );
		VisTextButton save = new VisTextButton( "Save" );
		
		back.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( new MainMenu() );
			}
		});
		
		save.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				Arrays.stream(settings).forEach( s -> {
					s.save(prefs);
				});		
				
				prefs.flush();
				
				StrategyMass.game.setScreen( new MainMenu() );
			}
		});
		
		Arrays.stream(settings).forEach( s -> {
			s.build(table, prefs);
			table.row();
		});
		table.add( back );
		table.add( save );
	}

	public static class Setting<E> {
		String name;
		String key;
		SettingType type;
		Object defaultValue;
		VisTextField field;

		public Setting( String name, String key, SettingType type, Object defaultValue ) {
			this.name = name;
			this.key = key;
			this.type = type;
			this.defaultValue = defaultValue;
		}

		public void build( Table table, Preferences prefs ) {
			field = new VisTextField();
			field.setText( type.get.get( prefs, key, defaultValue ) );

			table.add( new VisLabel( name + ":" ) );
			table.add( field );
		}

		public void save( Preferences prefs ) {
			type.set.set( prefs, key, field.getText().trim() );
		}
	}

	public enum SettingType {
		STRING( String.class, (prefs, key, defaultValue) -> prefs.getString(key, defaultValue), (prefs, key, v) -> prefs.putString( key, v ) ),
		INT( Integer.class, (prefs, key, defaultValue) -> String.valueOf(prefs.getInteger(key, defaultValue)), (prefs, key, v) -> prefs.putInteger( key, Integer.parseInt(v) ) );

		Getter get;
		Setter set;

		<E> SettingType( Class<E> c, Getter<E> get, Setter<E> set ) {
			this.get = get;
			this.set = set;
		}
	}

	public interface Setter<E> {
		public void set( Preferences prefs, String key, String value );
	}

	public interface Getter<E> {
		public String get( Preferences prefs, String key, E defaultValue );
	}
}
