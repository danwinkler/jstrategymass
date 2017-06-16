package com.danwink.libgdx.form;

public class SSelectBox<E> extends SElement
{
	E selected;
	E[] values;
	
	public SSelectBox( Object id )
	{
		super( id );
	}
	
	public Object serialize()
	{
		return new Object[] { selected, values };
	}

	public void setValues( E... values )
	{
		this.values = values;
	}
	
	public void setSelected( E selected )
	{
		this.selected = selected;
	}
	
	public E getSelected()
	{
		return selected;
	}
	
	public void change( int id ) {};
}