package com.irc.ircclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/*
 * Worker thread for handling IRC connections
 * Uses two request queues: One for connections,
 * one for messages
 */
public class IRCConnectionHandler extends Thread {

	// Connection request queue
	private ArrayBlockingQueue<ConnectionRequest> connectionRequests;
	private final int queueSize = 1024;
	private final int bufSize = 4096;
	
	// Indicates an error connection
	private ErrorConnection err;
	
	private boolean running = true;
	private HashMap<String, Connection> connections;
	
	// For multiplexing the socket connections
	private Selector readSelector;
	private Selector writeSelector;
	
	// For reading partial messages
	private IRCMessageAssembler assembler;
	private ByteBuffer buffer;
	
	public IRCConnectionHandler() throws IOException {
		readSelector = Selector.open();
		writeSelector = Selector.open();
		err = new ErrorConnection();
		connections = new HashMap<String, Connection>();
		connectionRequests = new ArrayBlockingQueue<ConnectionRequest>(queueSize);
		assembler = new IRCMessageAssembler();
		buffer = ByteBuffer.allocate(bufSize);
	}
	
	// Add a connection request
	public void addConnectionRequest(ConnectionRequest c) throws InterruptedException {
		connectionRequests.put(c);
	}
	
	@Override
	public void run() {
		while (isRunning()) {
			// Check for new connections
			checkConnections();
			// Next, we select sockets that might be ready for reading
			readSockets();
			// Next, we retrieve and parse any complete messages
			parseMessages();
		}
	}

	private void parseMessages() {
		
	}

	/*
	 * Read sockets that are available for reading
	 */
	private void readSockets() {
		try {
			int readableChannels = readSelector.selectNow();
			if (readableChannels > 0) {
				// Get all channels that can be read from
				Set<SelectionKey> selected = readSelector.selectedKeys();
				for (SelectionKey s : selected) {
					// Retrieve the id and channel
					String id = (String) s.attachment();
					SocketChannel channel = (SocketChannel) s.channel();
					// If socket read fails, that means that there is a connection error
					try {
						assembler.readMessage(id, channel, buffer);
					}
					catch (IOException e) {
						channel.close();
						connections.put(id, err);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Check for new connections
	 */
	private void checkConnections() {
		// First, we check for new connection request and connect them
		while (!connectionRequests.isEmpty()) {
			ConnectionRequest c = connectionRequests.poll();
			connectID(c.getId(), c.getHostname(), c.getPort());
		}
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}

	private void connectID(String id, String hostname, int port) {
		try {
			// Try connecting to the server
			System.out.println("New connection attempt, hostname: " + hostname + "port: " + port);
			SocketChannel newSocket = SocketChannel.open();
			// To use the selector to multiplex connections,
			// the socket needs to be non-blocking
			newSocket.configureBlocking(false);
			// Register with the selector
			SelectionKey s1 = newSocket.register(readSelector, SelectionKey.OP_READ);
			s1.attach(id);
			SelectionKey s2 = newSocket.register(writeSelector, SelectionKey.OP_WRITE);
			s2.attach(id);
			newSocket.connect(new InetSocketAddress(hostname, port));
			IRCConnection newConnection = new IRCConnection(newSocket);
			connections.put(id, newConnection);
		} catch (IOException e) {
			// Null value indicates error
			connections.put(id, err);
		}
	}
}
