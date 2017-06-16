package com.danwink.libgdx.form;

public abstract class SElement
{
	Object id;
	
	public SElement( Object id )
	{
		this.id = id;
	}
	
	public abstract Object serialize();
}
