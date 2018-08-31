package com.irc.ircclient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;

/*
 * A class for writing messages to a socket channel
 * Since message writes might not succeed fully,
 * we need to store partially written messages
 */
public class IRCMessageSender {

	private Selector writeSelector;
	private SocketChannel channel;
	private LinkedList<byte[]> messageQueue;
	private SelectionKey key;
	
	
	public IRCMessageSender(Selector writeSelector, SocketChannel channel) {
		this.writeSelector = writeSelector;
		this.channel = channel;
		this.messageQueue =  new LinkedList<byte[]>();
	}
	
	public synchronized void addMessage(byte[] msg, String id) throws IOException {
		// Register that we are ready to write and add message to queue
		if (key == null)
			key = channel.register(writeSelector, SelectionKey.OP_WRITE);
		key.attach(id);
		messageQueue.add(msg);
	}
	
	public void writeMessages() throws IOException {
		// Attempt to write the messages
		while (!messageQueue.isEmpty()) {
			// Get a message from the queue and attempt to write it
			byte[] byteMsg = messageQueue.poll();
			ByteBuffer curMsg = ByteBuffer.wrap(byteMsg);
			int numWritten = channel.write(curMsg);
			
			// Check if the whole message was written
			if (numWritten != byteMsg.length) {
				byte[] remaining = Arrays.copyOfRange(byteMsg, numWritten + 1, byteMsg.length);
				messageQueue.addFirst(remaining);
				break;
			}
		}
	}
}
