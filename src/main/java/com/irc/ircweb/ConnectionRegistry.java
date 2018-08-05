package com.irc.ircweb;

import java.util.ArrayList;
import java.util.HashMap;

import com.irc.ircclient.IRCConnectionHandler;

/*
 * Provides a mapping between websocket requests and
 * IRC connections from this server to the IRC server
 */
public class ConnectionRegistry {

	private HashMap<String, IRCConnectionHandler> handlerMap;
	
	// This is for picking a handler
	private IRCConnectionHandler handlers[];
	private int index = 0;
	
	public ConnectionRegistry(IRCConnectionHandler handlers[]) {
		this.handlerMap = new HashMap<String, IRCConnectionHandler>();
		this.handlers = handlers;
	}
	
	public IRCConnectionHandler getHandler(String id) {
		return handlerMap.get(id);
	}
	
	/*
	 * Register a handler for a connection
	 * @Param id the id of the session
	 */
	public void addSession(String id) {
		
	}
}
