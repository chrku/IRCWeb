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
	
	private final char DELIM_1 = '\r';
	private final char DELIM_2 = '\n';
	
	public IRCMessageAssembler() {
		partialMessages = new HashMap<String, byte[]>();
		messageQueue = new LinkedList<Message>();
	}
	
	/*
	 * Methods for retrieving messages
	 */
	public boolean messageQueueIsEmpty() {
		return messageQueue.isEmpty();
	}
	
	public Message pollMessageQueue() {
		return messageQueue.poll();
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
			parseMessage(newMsg, id);
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
	private void parseMessage(byte[] newMsg, String id) {
		// Look for the delimiter in messages
		int curIndex = 0;
		boolean delim1 = false;
		for (int i = 0; i < newMsg.length; ++i) {
			if (delim1 && (char) newMsg[i] == DELIM_2) {
				delim1 = false;
				// Allocate a new array and copy the message
				byte[] msg = new byte[i - curIndex];
				System.arraycopy(newMsg, curIndex, msg, 0, i - curIndex);
				messageQueue.add(new Message(id, msg));
				curIndex = i + 1;
			}
			else if ((char) newMsg[i] == DELIM_1) {
				delim1 = true;
			}
			else {
				delim1 = false;
			}
		}
		// If there is anything left over, we re-add to the map
		if (curIndex < newMsg.length) {
			byte[] msg = new byte[newMsg.length - curIndex];
			System.arraycopy(newMsg, curIndex, msg, 0, newMsg.length - curIndex);
			partialMessages.put(id, msg);
		}
	}
}
