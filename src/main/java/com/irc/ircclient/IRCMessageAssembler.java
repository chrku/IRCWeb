package com.irc.ircclient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;

/*
 * This reads in raw TCP streams, and converts 
 * them to discrete IRC messages. These messages can 
 * then be accessed by their ID
 */
public class IRCMessageAssembler {
	
	// Partial messages for each client
	private HashMap<String, byte[]> partialMessages;
	private LinkedList<Message> messageQueue;
	
	public IRCMessageAssembler() {
		partialMessages = new HashMap<String, byte[]>();
		messageQueue = new LinkedList<Message>();
	}
	
	/*
	 * Attempt to extract messages from the channel
	 */
	public void readMessage(String id, SocketChannel channel, ByteBuffer buf) throws IOException {
		
		// Read some data into the buffer
		channel.read(buf);
		buf.flip();
		
		// Read from the socket
		if (buf.remaining() > 0) {
			// Check if we have some partial message already
			byte[] newMsg;
			if (partialMessages.get(id) != null) {
				byte[] partial = partialMessages.get(id);
				newMsg = new byte[partial.length + buf.remaining()];
				// Copy the partial message into the new array
				System.arraycopy(partial, 0, newMsg, 0, partial.length);
				// ... and then the new message
				System.arraycopy(buf.array(), 0, newMsg, partial.length, buf.remaining());
				partialMessages.remove(id);
			}
			else {
				newMsg = new byte[buf.remaining()];
				System.arraycopy(buf.array(), 0, newMsg, 0, buf.remaining());
			}
			buf.clear();
			parseMessages(newMsg);
		}
		else {
			buf.clear();
		}
		
	}

	/*
	 * Attempt to extract IRC messages,
	 * delimited by \r\n from the read content
	 * @param newMsg the read content
	 */
	private void parseMessages(byte[] newMsg) {
		// 
	}
}
