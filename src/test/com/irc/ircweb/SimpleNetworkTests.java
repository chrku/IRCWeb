package com.irc.ircweb;

import java.io.IOException;
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
	
}
