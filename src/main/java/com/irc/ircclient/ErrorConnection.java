package com.irc.ircclient;

import java.nio.channels.SocketChannel;
import java.util.Queue;

public class ErrorConnection implements Connection {

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public SocketChannel getSocket() {
		return null;
	}

	@Override
	public Queue<IRCMessage> getMessageBuffer() {
		return null;
	}

	@Override
	public IRCMessage pollMessage() {
		return null;
	}

	@Override
	public void addMessage(IRCMessage message) {
	}

}
