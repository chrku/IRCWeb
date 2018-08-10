package com.irc.ircclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
 * Parses IRC messages, returning the exact message type and arguments
 * according to RFC 1459
 */
public class IRCMessageParser {
	
	// Provide a mapping between message codes
	// and our enum
	static HashMap<String, IRCMessageType> messageMap;
	static {
		messageMap = new HashMap<String, IRCMessageType>();
		messageMap.put("PING" , IRCMessageType.PING);
		messageMap.put("NOTIFY", IRCMessageType.NOTIFY);
	}
	
	
	/*
	 * Skip spaces in between arguments
	 */
	private int skipSpaces(int curIndex, byte[] message) {
		while (curIndex < message.length && message[curIndex] != ' ') {
			++curIndex;
		}
		
		return curIndex;
	}
	
	/*
	 * Skip the message prefix
	 */
	private int skipMessagePrefix(int curIndex, byte[] message) {
		// Messages can have an optional prefix
		// As a client, this can pretty much be ignored
		if (message[0] == ':') {
			// As we ignore the prefix, we simply skip to the next
			// delimiter
			while (curIndex < message.length && message[curIndex] != ' ') {
				++curIndex;
			}
			// and then skip the other spaces, as there can be arbitrarily 
			// many
			curIndex = skipSpaces(curIndex, message);
		}
		return curIndex;
	}
	
	private void parseArgs(int curIndex, byte[] message, ArrayList<String> args) {
		int argStart = curIndex;
		
		// According to RFC 1459, there are two types of parameters
		// normal ones and an optional trailing one that can contain
		// spaces.
		
		// Read until colon or CRLF
		while (curIndex < message.length && message[curIndex] != '\r'
			&& message[curIndex] != ':') {
			// Check for argument delimiter
			if (message[curIndex] == ' ') {
				String arg = new String(Arrays.copyOfRange(message, argStart, curIndex));
				args.add(arg);
				curIndex = skipSpaces(curIndex, message);
				argStart = curIndex;
			}
			else {
				++curIndex;
			}
		}
		
		// Now handle colon arguments separately
		if (message[curIndex] == ':') {
			++curIndex;
			if (curIndex < message.length) {
				
				argStart = curIndex;
				
				while (curIndex < message.length && message[curIndex] != '\r') {
					++curIndex;
				}
				
				String arg = new String(Arrays.copyOfRange(message, argStart, curIndex));
				args.add(arg);
			}
		}
	}
	
	public IRCMessage parseMessage(byte[] message) {

		int curIndex = 0;
		
		if (message.length == 0) {
			return new IRCMessage(null, null);
		}
		
		curIndex = skipMessagePrefix(curIndex, message);
		
		// Check if we reached the end
		if (curIndex >= message.length) {
			return new IRCMessage(null, null);
		}

		// Next comes the command string
		int cmdStart = curIndex;
		while (curIndex < message.length && message[curIndex] != ' ') {
			++curIndex;
		}
		
		// Now try to decode the command
		String command = new String(Arrays.copyOfRange(message, cmdStart, curIndex));
		
		IRCMessageType type = messageMap.get(command);
		
		// Check for valid message
		if (type == null) {
			return new IRCMessage(null, null);
		}

		curIndex = skipSpaces(curIndex, message);
		
		ArrayList<String> args = new ArrayList<String>();
		
		parseArgs(curIndex, message, args);
		
		return new IRCMessage(type, args);
	}
}
