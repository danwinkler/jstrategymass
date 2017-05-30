package com.danwink.strategymass.net;

public class Message
{
	public int sender = -1;
	public Object key;
	public Object value;
	
	public Message() {}
	
	public Message( Object key, Object value )
	{
		this.key = key;
		this.value = value;
	}
	
	public Message( Object key, Object value, int sender )
	{
		this.key = key;
		this.value = value;
		this.sender = sender;
	}
}
