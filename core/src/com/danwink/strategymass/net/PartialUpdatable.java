package com.danwink.strategymass.net;

public interface PartialUpdatable
{
	public void partialReadPacket( Object o );
	public Object partialMakePacket();
}
