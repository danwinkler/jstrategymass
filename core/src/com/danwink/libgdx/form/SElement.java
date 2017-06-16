package com.danwink.libgdx.form;

import com.badlogic.gdx.scenes.scene2d.Actor;

public abstract class SElement<F extends Actor>
{
	Object id;
	
	public SElement( Object id )
	{
		this.id = id;
	}
	
	public abstract Object serialize();
}
