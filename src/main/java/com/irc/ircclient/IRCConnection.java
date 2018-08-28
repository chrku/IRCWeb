package com.irc.ircclient;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/*
 * This represents an ongoing IRC connection, i.e.
 * the sockets and the message receive buffer
 */
public class IRCConnection {
	// Non-blocking socket
	private SocketChannel socket;
	private final int MAX_SIZE = 128;

	// Message buffer for messages
	private LinkedList<IRCMessage> messageBuffer;
	
	public IRCConnection(SocketChannel newSocket) {
		this.socket = newSocket;
		this.messageBuffer = new LinkedList<IRCMessage>();
	}

	public SocketChannel getSocket() {
		return socket;
	}

	public synchronized LinkedList<IRCMessage> getMessageBuffer() {
		return messageBuffer;
	}

	public boolean isConnected() {
		return true;
	}

	public IRCMessage pollMessage() {
		return messageBuffer.poll();
	}

	public void addMessage(IRCMessage message) {
		while (messageBuffer.size() > MAX_SIZE)
			messageBuffer.pop();
		messageBuffer.add(message);
	}
}
