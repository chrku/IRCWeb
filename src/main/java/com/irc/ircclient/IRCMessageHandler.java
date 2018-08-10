package com.irc.ircclient;

import java.io.IOException;
import java.util.HashMap;

/*
 * Handles IRC message, i.e. generates response messages
 * for the server and for the client
 */
public class IRCMessageHandler {
	
	private HashMap<String, Connection> connections;
	private HashMap<String, IRCMessageSender> senders;
	
	private static final byte[] MSG_SUFFIX = "\r\n".getBytes();
	private static final byte[] PING_RESPONSE = ("PONG" + MSG_SUFFIX).getBytes(); 
	
	public IRCMessageHandler(HashMap<String, Connection> connections, HashMap<String, IRCMessageSender> senders) {
		this.connections = connections;
		this.senders = senders;
	} 
	
	public void handleMessage(String id, IRCMessage msg) throws IOException {
		// Determine what to do based on the message type
		switch (msg.getType()) {
		case NOTIFY:
			break;
		case PING:
			senders.get(id).addMessage(PING_RESPONSE, id);
			break;
		default:
			break;
		}
	}
	
}
