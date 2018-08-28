package com.irc.ircweb;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irc.ircclient.ConnectionRequest;
import com.irc.ircclient.IRCConnectionHandler;
import com.irc.ircclient.IRCMessage;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.web.socket.CloseStatus;
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
		// int numCores = Runtime.getRuntime().availableProcessors();
		int numCores = 1;
		// Create handler threads
		handlers = new IRCConnectionHandler[numCores];
		for (int i = 0; i < handlers.length; ++i) {
			handlers[i] = new IRCConnectionHandler();
			handlers[i].start();
		}
		
		// Create registry and message creator
		registry = new ConnectionRegistry(handlers);
		creator = new WSMessageCreator();
		
		// Create mapper to decode JSON
		mapper = new ObjectMapper();
	}
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage msg) throws IOException, InterruptedException {
		// Attempt to read JSON data
		// System.out.println("New message:" + new String(msg.asBytes()));
		JsonNode jsonData = mapper.readTree(msg.asBytes());
		
		// Attempt to get the type of the message
		JsonNode messageType = jsonData.get("type");
		IRCConnectionHandler handler = registry.getHandler(session.getId());
		String id = session.getId();

		switch(messageType.asText()) {
		case "PING":
			session.sendMessage(creator.generatePong());
			break;
		case "DISCONNECT":
			handler.removeConnection(id);
			registry.removeSession(id);
			session.close();
			break;
		case "CONNECTION-ATTEMPT":
			// Get the server data to connect to
			String hostname = jsonData.get("hostname").asText();
			int port = jsonData.get("port").asInt();
			System.out.println("Attempting to connect to connection: " + hostname + ":" + port);
			// Check for existing connection
			if (handler.getConnection(id) == null)
				handler.addConnectionRequest(new ConnectionRequest(session.getId(), hostname, port));
			break;
		case "SEND-IRC-MESSAGE":
			// Check if the connection has not yet been processed
			if (handler.isPending(id) == true) {
				session.sendMessage(creator.generateConnectionPending());
				return;
			}
			if (handler.getConnection(id) == null) {
				session.sendMessage(creator.generateConnectionError());
				return;
			}

			break;
		case "READ-MESSAGES":
			// Check if the connection has not yet been processed
			if (handler.isPending(id) == true) {
				session.sendMessage(creator.generateConnectionPending());
				return;
			}
			if (handler.getConnection(id) == null) {
				session.sendMessage(creator.generateConnectionError());
				return;
			}
			
			if (handler.getConnection(id).getMessageBuffer().isEmpty()) {
				session.sendMessage(creator.noNewMessages());
			}
			else {
				ArrayList<IRCMessage> newMessages = new ArrayList<IRCMessage>();
				
				while (!handler.getConnection(id).getMessageBuffer().isEmpty()) {
					newMessages.add(handler.getConnection(id).getMessageBuffer().poll());
				} 
				
				session.sendMessage(creator.newMessages(newMessages));
			}
			break;
		}
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws IOException {
		// Register session
		registry.addSession(session.getId());
		System.out.println("Added session with ID: " + session.getId());
		// Signal connection success to session
		session.sendMessage(creator.generateSuccess());
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
		System.out.println("Closing session with ID: " + session.getId());
		IRCConnectionHandler handler = registry.getHandler(session.getId());
		if (handler == null)
			System.out.println("ERROR");
		handler.removeConnection(session.getId());
		registry.removeSession(session.getId());
	}
	
	@Override
	public void handleTransportError(WebSocketSession session, java.lang.Throwable exception) {
		IRCConnectionHandler handler = registry.getHandler(session.getId());
		handler.removeConnection(session.getId());
		registry.removeSession(session.getId());
	}
	
	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
}
