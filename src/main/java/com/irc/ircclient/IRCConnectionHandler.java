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
	private HashMap<String, IRCMessageSender> senders;
	
	// For multiplexing the socket connections
	private Selector readSelector;
	private Selector writeSelector;
	
	// For reading partial messages
	private IRCMessageAssembler assembler;
	private ByteBuffer buffer;
	
	// For parsing and handling messages
	private IRCMessageParser parser;
	private IRCMessageHandler handler;
	
	public IRCConnectionHandler() throws IOException {
		this.readSelector = Selector.open();
		this.writeSelector = Selector.open();
		this.err = new ErrorConnection();
		this.connections = new HashMap<String, Connection>();
		this.connectionRequests = new ArrayBlockingQueue<ConnectionRequest>(queueSize);
		this.assembler = new IRCMessageAssembler();
		this.buffer = ByteBuffer.allocate(bufSize);
		this.senders = new HashMap<String, IRCMessageSender>();
		this.parser = new IRCMessageParser();
		this.handler = new IRCMessageHandler(connections, senders);
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
			parseAndHandleMessages();
			// Next, we select sockets that have messages 
			// pending to send and try to send them
			sendPendingMessages();
		}
	}

	private void sendPendingMessages() {
		int amount;
		try {
			amount = writeSelector.selectNow();
		} catch (IOException e1) {
			return;
		}
		if (amount > 0) {
			Set<SelectionKey> selected = writeSelector.selectedKeys();
			for (SelectionKey s : selected) {
				String id = (String) s.attachment();
				IRCMessageSender sender = senders.get(id);
				try {
					sender.writeMessages();
				} catch (IOException e) {
					removeConnection(id);
				}
			}
		}
		
	}

	public void removeConnection(String id) {
		senders.remove(id);
		if (connections.containsKey(id)) {
			try {
				connections.get(id).getSocket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		connections.put(id, err);
	}
	
	private void parseAndHandleMessages() {
		// Handle all the newly assembled messages
		while (!assembler.messageQueueIsEmpty()) {
			Message msg = assembler.pollMessageQueue();
			IRCMessage message = parser.parseMessage(msg.getContent());
			try {
				handler.handleMessage(msg.getId(), message);
			} catch (IOException e) {
				removeConnection(msg.getId());
			}
		}
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
						removeConnection(id);
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
			
			// Create new message sender
			IRCMessageSender sender = new IRCMessageSender(writeSelector, newSocket);
			senders.put(id, sender);
			
			// Connect the socket channel
			newSocket.connect(new InetSocketAddress(hostname, port));
			IRCConnection newConnection = new IRCConnection(newSocket);
			connections.put(id, newConnection);
		} catch (IOException e) {
			// Null value indicates error
			removeConnection(id);
		}
	}
}
