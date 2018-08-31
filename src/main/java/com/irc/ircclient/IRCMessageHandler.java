package com.irc.ircclient;

import java.io.IOException;
import java.util.HashMap;

/*
 * Handles IRC message, i.e. generates response messages
 * for the server and for the client
 */
public class IRCMessageHandler {
	
	private HashMap<String, IRCConnection> connections;
	private HashMap<String, IRCMessageSender> senders;
	
	private static final byte[] PING_RESPONSE = "PONG\r\n".getBytes(); 
	
	public IRCMessageHandler(HashMap<String, IRCConnection> connections, HashMap<String, IRCMessageSender> senders) {
		this.connections = connections;
		this.senders = senders;
	} 
	
	public void handleMessage(String id, IRCMessage msg) throws IOException {
		// Determine what to do based on the message type
		switch (msg.getType()) {
		case "PING":
			senders.get(id).addMessage(PING_RESPONSE, id);
			break;
		default:
			connections.get(id).addMessage(msg);
			break;
		}
	}
	
}
