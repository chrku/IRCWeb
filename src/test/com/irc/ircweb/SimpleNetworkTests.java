package com.irc.ircweb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.irc.ircclient.*;



/*
 * Test suite to verify that simple network functionality works
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SimpleNetworkTests {

	// Very simple, send and receive a few bytes
	@Test
	public void simpleSendReceive() {
		try {
			int portNum = 7885;
			SimpleEchoServer newServ = new SimpleEchoServer(portNum);
			newServ.start();
			IRCSocket client = new IRCSocket("localhost", portNum);
			client.sendMsg("hello world".getBytes());
			client.recvMsg();
			newServ.setRunning(false);
			String out = new String (client.getBuffer(), Charset.forName("utf-8")).trim();
			Assert.assertEquals(out, "hello world");
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Connection failed");
		}
	}
	
	// Test if malformed nicknames work
	
	// Test for max length of 9
	@Test(expected = IRCInvalidFormatException.class)
	public void testNickLength() throws UnsupportedEncodingException, IRCInvalidFormatException {
		CommandCreator creator = new CommandCreator();
		@SuppressWarnings("unused")
		byte[] nick_cm = creator.generateNick("aaaaaaaaaa");
	}
	
	// Test invalid characters
	@Test(expected = IRCInvalidFormatException.class)
	public void testNickFormatSpecial() throws UnsupportedEncodingException, IRCInvalidFormatException {
		CommandCreator creator = new CommandCreator();
		@SuppressWarnings("unused")
		byte[] nick_cm = creator.generateNick("##üäüä");
	}
	
	// Test valid characters
	@SuppressWarnings("unused")
	@Test()
	public void testNickFormatValid() throws UnsupportedEncodingException, IRCInvalidFormatException {
		CommandCreator creator = new CommandCreator();
		byte[] nick_cm1 = creator.generateNick("a_");
		byte[] nick_cm2 = creator.generateNick("a1_");
		byte[] nick_cm3 = creator.generateNick("gdf-_");
		byte[] nick_cm4 = creator.generateNick("a{{12_");
		byte[] nick_cm5 = creator.generateNick("aadf_");
		byte[] nick_cm6 = creator.generateNick("afddafs");
		byte[] nick_cm7 = creator.generateNick("assd1");
		byte[] nick_cm8 = creator.generateNick("a___1");
		byte[] nick_cm9 = creator.generateNick("o_O");
		byte[] nick_cm10 = creator.generateNick("dfsa123");
		byte[] nick_cm11 = creator.generateNick("a331");
		byte[] nick_cm12 = creator.generateNick("a12a");
	}
	
	// Test invalid characters in username
		@Test(expected = IRCInvalidFormatException.class)
		public void testUserFormatSpecial() throws UnsupportedEncodingException, IRCInvalidFormatException {
			CommandCreator creator = new CommandCreator();
			@SuppressWarnings("unused")
			byte[] nick_cm = creator.generateUser("    ", "john doe");
		}
}
