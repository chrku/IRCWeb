package com.irc.ircclient;

import java.nio.channels.SocketChannel;
import java.util.Queue;

/*
 * This represents an ongoing IRC connection, i.e.
 * the sockets and the message receive buffer
 */
public class IRCConnection implements Connection {
	// Non-blocking socket
	private SocketChannel socket;

	// Message buffer for messages
	private Queue<String> messageBuffer;
	
	public IRCConnection(SocketChannel newSocket) {
		this.socket = newSocket;
	}

	@Override
	public SocketChannel getSocket() {
		return socket;
	}

	@Override
	public Queue<String> getMessageBuffer() {
		return messageBuffer;
	}

	@Override
	public boolean isConnected() {
		return true;
	}
}
