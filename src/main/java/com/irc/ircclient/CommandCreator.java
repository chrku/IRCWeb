package com.irc.ircclient;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Creates valid IRC commands to send 
 * off using the socket
 */
public class CommandCreator {

	// Validation and protocol internals
	private final String IRC_SUFFIX = "\r\n";
	private final String NICK_PREFIX = "NICK ";
	private final String USER_PREFIX = "USER ";
	private final String JOIN_PREFIX = "JOIN ";
	private final String QUIT_PREFIX = "QUIT ";
	private final int NICK_MAX_LENGTH = 9;
	
	// Validation patters
	private Pattern nickName;
	private Pattern IRCString;
	private Pattern channelString;
	
	// Server and host name
	// Not really important in client protocol, 
	// so they are set constant here
	
	private final String HOST = "webirc";
	private final String SERVER = "webirc";
	
	// Maximum command length
	private final int MAX_LEN_CMD = 500;
	
	public CommandCreator() {
		nickName = Pattern.compile("([A-Z]|[a-z])([A-Z]|[a-z]|[0-9]|\\-|\\[|\\]|\\\\|`|\\^|\\{|\\}|_)*");
		IRCString = Pattern.compile("[^\\n\\r ,\\x00]+");
		channelString = Pattern.compile("(#|&)[^\\n\\r ,\\x00]+");
	}
	
	/*
	 * Generate a nickname command, to be used
	 * to specify a nickname when connecting to a
	 * server
	 */
	public byte[] generateNick(String nick) throws IRCInvalidFormatException, UnsupportedEncodingException {
		Matcher nickMatcher = nickName.matcher(nick);
		if (!nickMatcher.matches() || nick.length() > NICK_MAX_LENGTH)
			throw new IRCInvalidFormatException();
		else 
			return (NICK_PREFIX + nick + IRC_SUFFIX).getBytes("ascii");
	}
	
	/*
	 * Generate a user command, to be used
	 * to specify a nickname when connecting to a
	 * server
	 */
	public byte[] generateUser(String user, String real)
			throws UnsupportedEncodingException, IRCInvalidFormatException {
		Matcher userMatcher = IRCString.matcher(user);
		String msg = USER_PREFIX + user + " " + HOST + " " + SERVER + " :" + real + IRC_SUFFIX;
		if (msg.length() > MAX_LEN_CMD || !userMatcher.matches())
			throw new IRCInvalidFormatException();
		return msg.getBytes("ascii");
	}
	
	/*
	 * Generate a command to join a channel on a server
	 */
	public byte[] generateJoin(String channel) throws IRCInvalidFormatException, UnsupportedEncodingException {
		Matcher channelMatcher = channelString.matcher(channel);
		String msg = JOIN_PREFIX + channel + IRC_SUFFIX;
		if (msg.length() > MAX_LEN_CMD || !channelMatcher.matches())
			throw new IRCInvalidFormatException();
		return msg.getBytes("ascii");
	}
	
	public byte[] generateQuit(String channel) {
		return null;	
	}
	
}
