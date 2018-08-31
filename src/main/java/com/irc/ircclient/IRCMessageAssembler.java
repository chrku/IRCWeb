package com.irc.ircclient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;

/*
 * This reads in raw TCP streams, and converts 
 * them to discrete IRC messages. These messages can 
 * then be accessed by their ID. Partial messages
 * are saved and assembled once they are complete.
 * Completeness is gauged by the presence of the 
 * delimiter '\r\n'
 */
public class IRCMessageAssembler {
	
	// Partial messages for each client
	private HashMap<String, byte[]> partialMessages;
	private LinkedList<Message> messageQueue;
	
	// Message delimiters
	private final char DELIM_1 = '\r';
	private final char DELIM_2 = '\n';
	
	public IRCMessageAssembler() {
		partialMessages = new HashMap<String, byte[]>();
		messageQueue = new LinkedList<Message>();
	}
	
	/*
	 * Methods for retrieving messages
	 */
	
	/*
	 * Indicates whether any complete messages are available for
	 * use
	 * @return boolean status of queue
	 */
	public boolean messageQueueIsEmpty() {
		return messageQueue.isEmpty();
	}
	
	/*
	 * Gets the front-most message of the queue and returns it
	 * The message is removed from the queue
	 * @return Message front-most completed message
	 */
	public Message pollMessageQueue() {
		return messageQueue.poll();
	}
	
	/*
	 * Attempt to extract messages from the given socket
	 * into the given buffer. If the message is not complete
	 * it is saved internally. Complete messages are added
	 * to the queue and can be read with pollMessageQueue()
	 * @param id the ID of the message
	 * @param channel socket to be read from
	 * @param buf buffer to read into
	 * @exception IOException when socket cannot be read from
	 */
	public void readMessage(String id, SocketChannel channel, ByteBuffer buf) throws IOException {
		
		// Read some data into the buffer
 		buf.clear();
 		while (channel.read(buf) > 0) {
			buf.flip();
			// Read from the socket
			if (buf.remaining() > 0) {
				// Check if we have some partial message already
				byte[] newMsg;
				if (partialMessages.get(id) != null) {
					
					byte[] partial = partialMessages.get(id);
					newMsg = new byte[partial.length + buf.limit()];
					// Copy the partial message into the new array
					System.arraycopy(partial, 0, newMsg, 0, partial.length);
	
					// ... and then the new message
					buf.get(newMsg, partial.length, buf.limit());
					partialMessages.remove(id);
				}
				else {
					newMsg = new byte[buf.limit()];
					buf.get(newMsg, 0, buf.limit());
				}
				buf.clear();
				parseMessage(newMsg, id);
			}
			else {
				buf.clear();
			}
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
		System.out.println("[IRCMessageAssembler] Trying to parse message: " + new String(newMsg));
		for (int i = 0; i < newMsg.length; ++i) {
			if (delim1 && (char) newMsg[i] == DELIM_2) {
				delim1 = false;
				// Allocate a new array and copy the message
				byte[] msg = new byte[i - curIndex  + 1];
				System.arraycopy(newMsg, curIndex, msg, 0, i - curIndex + 1);
				messageQueue.add(new Message(id, msg));
				curIndex = i + 1;
				System.out.println("[IRCMessageAssembler] Entire new message read: " + new String(msg));
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
