package com.danwink.libgdx.form;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class STextButton extends SElement<TextButton>
{
	String text;
	
	public STextButton( Object id )
	{
		super( id );
	}

	public void click( int id ) {}

	public void setText( String text )
	{
		this.text = text;
	}

	public Object serialize()
	{
		return text;
	}
}