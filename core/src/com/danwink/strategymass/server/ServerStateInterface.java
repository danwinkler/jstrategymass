package com.danwink.strategymass.server;

import com.danwink.dsync.DServer;

public interface ServerStateInterface
{
	public void register( DServer server );
	public void show();
	public void update( float dt );
}
