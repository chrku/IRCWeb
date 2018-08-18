package com.irc.ircclient;

import java.nio.channels.SocketChannel;
import java.util.Queue;

public interface Connection {
	public boolean isConnected();
	public SocketChannel getSocket();
	public Queue<IRCMessage> getMessageBuffer();
	public IRCMessage pollMessage();
	public void addMessage(IRCMessage message);
}
