package com.irc.ircweb;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private ObjectMapper mapper;
	
	// Worker threads
	private IRCConnectionHandler handlers[];
	
	public WSHandler() throws IOException {
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
		
		// Create mapper to decode JSON
		mapper = new ObjectMapper();
	}
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage msg) throws IOException {
		// Attempt to read JSON data
		JsonNode jsonData = mapper.readTree(msg.asBytes());
		
		// Attempt to get the type of the message
		JsonNode messageType = jsonData.get("type");
		switch(messageType.asText()) {
		case "PING":
			session.sendMessage(creator.generatePong());
			break;
		case "CONNECTION-ATTEMPT":
			break;
		}
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws IOException {
		// Register session
		registry.addSession(session.getId());
		// Signal connection success to session
		session.sendMessage(creator.generateSuccess());
	}
}
