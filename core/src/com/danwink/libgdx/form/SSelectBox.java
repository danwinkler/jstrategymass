package com.danwink.libgdx.form;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;

public class SSelectBox<E> extends SElement<SelectBox<E>>
{
	String current;
	String[] values;
	
	public SSelectBox( Object id )
	{
		super( id );
	}
	
	public void set( SSelectBox<E> el )
	{
		
	}

	public void update( SelectBox<E> a )
	{
		
	}

	public Object serialize()
	{
		return new Object[] { current, values };
	}
}