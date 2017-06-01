package com.danwink.strategymass.net;

public interface PartialUpdatable<E>
{
	public void partialReadPacket( E e );
	public E partialMakePacket();
}
