package com.irc.ircclient;

import java.util.ArrayList;
import java.util.Arrays;

/*
 * Parses IRC messages, returning the exact message type and arguments
 * according to RFC 1459
 */
public class IRCMessageParser {
	
	/*
	 * Skip spaces in between arguments
	 * 
	 * @param curIndex current index
	 * @param message Message to be parsed
	 * @return int Index after skipping spaces
	 */
	private int skipSpaces(int curIndex, byte[] message) {
		while (curIndex < message.length && message[curIndex] == ' ') {
			++curIndex;
		}
		
		return curIndex;
	}
	
	/*
	 * Handle the message prefix, which indicates the message origin
	 * 
	 * @param curIndex current index in the message
	 * @param message message to be parsed
	 * @param args Arguments of the message
	 * @return int Index after parsing
	 */
	private int handleMessagePrefix(int curIndex, byte[] message, IRCMessage newMessage) {
		// Messages can have an optional prefix
		// As a client, this can pretty much be ignored
		if (message[curIndex] == ':') {
			// As we ignore the prefix, we simply skip to the next
			// delimiter
			curIndex++;
			int argStart = curIndex;
			while (curIndex < message.length && message[curIndex] != ' ') {
				++curIndex;
			}
			// Extract the name and add it as a first argument
			String arg = new String(Arrays.copyOfRange(message, argStart, curIndex));
			newMessage.setSender(arg);
			
			// and then skip the other spaces, as there can be arbitrarily 
			// many
			curIndex = skipSpaces(curIndex, message);
		}
		else {
			// Add empty sender in absence
			newMessage.setSender("");
		}
		return curIndex;
	}
	
	/*
	 * Parse message arguments, including the trailing argument
	 * 
	 * @param curIndex current index in the message
	 * @param message message to be parsed
	 * @param args message arguments
	 */
	private void parseArgs(int curIndex, byte[] message, IRCMessage newMessage) {

		curIndex = skipSpaces(curIndex, message);
		
		ArrayList<String> args = new ArrayList<String>();
		
		int argStart = curIndex;
		
		// According to RFC 1459, there are two types of parameters
		// normal ones and an optional trailing one that can contain
		// spaces.
		
		// Read until colon or CRLF
		while (curIndex < message.length) {
			// Check for argument delimiter
			if (message[curIndex] == ' ' || message[curIndex] == '\r'
					|| message[curIndex] == ':') {
				if (curIndex != argStart) {
					String arg = new String(Arrays.copyOfRange(message, argStart, curIndex));
					args.add(arg);
				}
				if (message[curIndex] == '\r' || message[curIndex] == ':')
					break;
				curIndex = skipSpaces(curIndex, message);
				argStart = curIndex;
			}
			else {
				++curIndex;
			}
		}
		
		// Now handle colon arguments separately
		if (curIndex < message.length && message[curIndex] == ':') {
			++curIndex;
			if (curIndex < message.length) {
				
				argStart = curIndex;
				
				while (curIndex < message.length && message[curIndex] != '\r') {
					++curIndex;
				}
				
				String arg = new String(Arrays.copyOfRange(message, argStart, curIndex));
				newMessage.setTrailer(arg);
			}
		}
		newMessage.setArgs(args);
	}
	
	/*
	 * Parse an IRC message, returning a type that 
	 * contains the command and the arguments
	 * 
	 * @param message the raw message
	 * @return IRCMessage the parsed message
	 */
	public IRCMessage parseMessage(byte[] message) {

		int curIndex = 0;
		IRCMessage newMessage = new IRCMessage();
		
		if (message.length == 0) {
			return new IRCMessage(null, null, null, null);
		}
		
		curIndex = handleMessagePrefix(curIndex, message, newMessage);
		
		// Check if we reached the end
		if (curIndex >= message.length) {
			return new IRCMessage(null, null, null, null);
		}

		// Next comes the command string
		int cmdStart = curIndex;
		while (curIndex < message.length && message[curIndex] != ' ' && message[curIndex] != '\r') {
			++curIndex;
		}
		
		// Now try to decode the command
		String command = new String(Arrays.copyOfRange(message, cmdStart, curIndex));
		
		// Check for valid message
		if (command == "") {
			return new IRCMessage(null, null, null, null);
		}
		
		newMessage.setType(command);
		parseArgs(curIndex, message, newMessage);
		
		return newMessage;
	}
}
