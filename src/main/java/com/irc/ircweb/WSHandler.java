package com.irc.ircweb;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.irc.ircclient.IRCConnectionHandler;

import java.io.IOException;

import org.springframework.web.socket.TextMessage;

/*
 * Handles incoming websocket messages
 * Uses worker threads to process messages
 */
public class WSHandler extends TextWebSocketHandler {
	
	// Helper classes for handling messages
	private ConnectionRegistry registry;
	private WSMessageCreator creator;
	
	// Worker threads
	private IRCConnectionHandler handlers[];
	
	public WSHandler() {
		// Get number of cores
		int numCores = Runtime.getRuntime().availableProcessors();
		// Create handler threads
		handlers = new IRCConnectionHandler[numCores];
		for (IRCConnectionHandler h : handlers) {
			h = new IRCConnectionHandler();
			h.start();
		}
		
		// Create registry and message creator
		registry = new ConnectionRegistry(handlers);
		creator = new WSMessageCreator();
	}
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage msg) {
		
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws IOException {
		// Register session
		registry.addSession(session.getId());
		// Signal connection success to session
		session.sendMessage(creator.generateSuccess());
	}
}
