package com.irc.ircclient;

import java.nio.channels.SocketChannel;
import java.util.Queue;

public interface Connection {
	public boolean isConnected();
	public SocketChannel getSocket();
	public Queue<String> getMessageBuffer();
}
