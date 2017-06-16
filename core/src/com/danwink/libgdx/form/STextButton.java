package com.danwink.libgdx.form;

public class STextButton extends SElement
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