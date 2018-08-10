package com.irc.ircclient;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/*
 * This represents an ongoing IRC connection, i.e.
 * the sockets and the message receive buffer
 */
public class IRCConnection implements Connection {
	// Non-blocking socket
	private SocketChannel socket;

	// Message buffer for messages
	private LinkedList<String> messageBuffer;
	
	public IRCConnection(SocketChannel newSocket) {
		this.socket = newSocket;
		this.messageBuffer = new LinkedList<String>();
	}

	@Override
	public SocketChannel getSocket() {
		return socket;
	}

	@Override
	public LinkedList<String> getMessageBuffer() {
		return messageBuffer;
	}

	@Override
	public boolean isConnected() {
		return true;
	}
}
