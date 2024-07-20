package com.rrd.c1ux.api.services.common.exception;
import java.util.Collection;

@SuppressWarnings("serial")
public class CORPCException extends Exception
{
	private Collection<String> messageList;

	private CORPCException() {}
	
	public CORPCException(String message)
	{
		super(message);
	}
	
	public CORPCException(String message, Collection<String> messageList)
	{
		super(message);
		this.messageList = messageList;
	}
	
	public Collection<String> getMessageList()
	{
		return messageList;
	}
	
}

