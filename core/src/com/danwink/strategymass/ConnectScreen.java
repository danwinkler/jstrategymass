package com.danwink.strategymass;

import java.net.InetAddress;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.danwink.strategymass.server.GameServer;
import com.esotericsoftware.kryonet.Client;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;

public class ConnectScreen extends MenuScreen
{
	public void build()
	{
		VisTextField addr = new VisTextField();
		VisTextButton connect = new VisTextButton( "Connect" );
		VisTextButton cancel = new VisTextButton( "Back" );
		VisTextButton searchLan = new VisTextButton( "Search LAN" );
		VisLabel message = new VisLabel();
		
		table.add( new VisLabel( "Address:" ) ).padRight( 10 ).padBottom( 10 );
		table.add( addr ).colspan( 2 ).padBottom( 10 ).fillX().padRight( 10 ).minWidth( 250 );
		table.add( searchLan ).padBottom( 10 );
		table.row();
		table.add();
		table.add( cancel ).width( 100 ).left();
		table.add( connect ).width( 140 ).left();
		table.row();
		table.add();
		table.add( message ).colspan( 4 ).left();
		
		connect.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				Screens.connected.setAddress( addr.getText().trim() );
				StrategyMass.game.setScreen( Screens.connected );
			}
		});
		
		cancel.addListener( new ClickListener(){
			public void clicked( InputEvent e, float x, float y ) {
				StrategyMass.game.setScreen( Screens.mainMenu );
			}
		});
		
		searchLan.addListener( new ChangeListener() {
			public void changed( ChangeEvent event, Actor actor )
			{
				message.setText( "Searching for server..." );
				new Thread( () -> {
					Client c = new Client();
					InetAddress host = c.discoverHost( GameServer.UDP_PORT, 2000 );
					if( host != null )
					{
						addr.setText( host.getHostAddress() );
						message.setText( "Found Server!" );
					}
					else
					{
						message.setText( "Couldn't find server running on local network." );
					}
				}).start();
			}
		});
	}
}
