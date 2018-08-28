package com.irc.ircclient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/*
 * Test suite for the message parser
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes=com.irc.ircweb.IrcwebApplication.class)
public class IRCMessageParserTests {

	IRCMessageParser parser;
	
	@Before
	public void setup() {
		parser = new IRCMessageParser();
	}
	
	/*
	 * Tests whether PING message gets parsed correctly
	 */
	@Test 
	public void testPingMessage() {
		byte[] msg = "PING\r\n".getBytes();
		IRCMessage parsedMessage = parser.parseMessage(msg);
		Assert.assertEquals("PING", parsedMessage.getType());
	}
	
	/*
	 * Tests how malformed message gets parsed,
	 * i.e. whether it goes out of bounds
	 */
	@Test 
	public void testMalformedMessage() {
		byte[] msg = "PING".getBytes();
		@SuppressWarnings("unused")
		IRCMessage parsedMessage = parser.parseMessage(msg);
	}
	
	/*
	 * Tests how message with arguments gets parsed
	 */
	@Test 
	public void testMessageArguments() {
		byte[] msg = "PRIVMSG Angel\r\n".getBytes();
		IRCMessage parsedMessage = parser.parseMessage(msg);
		
		Assert.assertEquals("PRIVMSG", parsedMessage.getType());
		Assert.assertEquals("", parsedMessage.getArgs().get(0));
		Assert.assertEquals("Angel", parsedMessage.getArgs().get(1));
	}
	
	/*
	 * Tests how message with trailing arguments gets parsed
	 */
	@Test 
	public void testMessageArgumentsTrailing() {
		byte[] msg = "PRIVMSG Angel :yes\r\n".getBytes();
		IRCMessage parsedMessage = parser.parseMessage(msg);
		
		Assert.assertEquals("PRIVMSG", parsedMessage.getType());
		Assert.assertEquals("", parsedMessage.getArgs().get(0));
		Assert.assertEquals("Angel", parsedMessage.getArgs().get(1));
		Assert.assertEquals("yes", parsedMessage.getArgs().get(2));
	}
	
	/*
	 * Tests how message with prefix arguments gets parsed
	 */
	@Test 
	public void testMessageArgumentsPrefix() {
		byte[] msg = ":Angel PRIVMSG Wiz :Hello are you receiving this message ?\r\n".getBytes();
		IRCMessage parsedMessage = parser.parseMessage(msg);
		
		Assert.assertEquals("PRIVMSG", parsedMessage.getType());
		Assert.assertEquals("Angel", parsedMessage.getArgs().get(0));
		Assert.assertEquals("Wiz", parsedMessage.getArgs().get(1));
		Assert.assertEquals("Hello are you receiving this message ?", parsedMessage.getArgs().get(2));
	}
}
