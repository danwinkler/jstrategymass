package com.danwink.strategymass.net;

public abstract class SyncObject<E>
{
	public int syncId;
	public boolean update;
	public boolean partial;
	public boolean remove;
	
	public abstract void set( E so );
}
