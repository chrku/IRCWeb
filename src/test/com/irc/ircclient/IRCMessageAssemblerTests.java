package com.irc.ircclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Assert;
import org.junit.Before;

/*
 * Test suite for the message assembler
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes=com.irc.ircweb.IrcwebApplication.class)
public class IRCMessageAssemblerTests {
	
	SimpleEchoServer server;
	
	/*
	 * Set up a test server
	 */
	@Before
	public void setUpServer() throws IOException {
		server = new SimpleEchoServer(0);
		server.start();
	}
	
	/*
	 * Test whether a simple message can be read
	 */
	@Test
	public void testSimpleMessage() throws IOException {
		// Set up channel and connect it to the server
		IRCMessageAssembler assembler = new IRCMessageAssembler();
		SocketChannel channel = SocketChannel.open();
		channel.connect(new InetSocketAddress("localhost", server.getPortNumber()));
		channel.configureBlocking(true);
		
		// Write a message to the echo server
		ByteBuffer newBuffer = ByteBuffer.allocate(1024);
		ByteBuffer buf = ByteBuffer.allocate(1024);
		newBuffer.put("EXAMPLE MESSAGE \n\r\n".getBytes());
		newBuffer.flip();
		channel.write(newBuffer);
		
		assembler.readMessage("0", channel, buf);
		
		// Read from the assembler and test whether the message
		// was handled correctly
		Message msg = assembler.pollMessageQueue();
		
		Assert.assertEquals(msg.getId(), "0");
		Assert.assertArrayEquals(msg.getContent(), "EXAMPLE MESSAGE \n\r\n".getBytes());
	}
	
	/*
	 * Test whether simple non-ascii message works
	 */
	@Test
	public void testSimpleNonAsciiMessage() throws IOException {
		// Set up channel and connect it to the server
		IRCMessageAssembler assembler = new IRCMessageAssembler();
		SocketChannel channel = SocketChannel.open();
		channel.connect(new InetSocketAddress("localhost", server.getPortNumber()));
		channel.configureBlocking(true);
		
		// Write a message to the echo server
		ByteBuffer newBuffer = ByteBuffer.allocate(1024);
		ByteBuffer buf = ByteBuffer.allocate(1024);
		newBuffer.put("岡ト毎援じ覧計 \r\n".getBytes());
		newBuffer.flip();
		channel.write(newBuffer);
		
		assembler.readMessage("0", channel, buf);
		
		// Read from the assembler and test whether the message
		// was handled correctly
		Message msg = assembler.pollMessageQueue();
		
		Assert.assertEquals(msg.getId(), "0");
		Assert.assertArrayEquals(msg.getContent(), "岡ト毎援じ覧計 \r\n".getBytes());
	}
	
	/*
	 *  Test multiple non-ascii messages
	 */
	@Test
	public void testSimpleNonAsciiMessages() throws IOException {
		// Set up channel and connect it to the server
		IRCMessageAssembler assembler = new IRCMessageAssembler();
		SocketChannel channel = SocketChannel.open();
		channel.connect(new InetSocketAddress("localhost", server.getPortNumber()));
		channel.configureBlocking(true);
		
		// Write a message to the echo server
		ByteBuffer newBuffer = ByteBuffer.allocate(1024);
		ByteBuffer buf = ByteBuffer.allocate(1024);
		newBuffer.put("岡ト毎援じ覧計 \r\n".getBytes());
		newBuffer.flip();
		channel.write(newBuffer);
		newBuffer.clear();
		newBuffer.put("aaaaabbbb\r\n".getBytes());
		newBuffer.flip();
		//System.out.println("Buffer before send:" +  new String(newBuffer.array()));
		channel.write(newBuffer);
		
		assembler.readMessage("0", channel, buf);
		
		// Read from the assembler and test whether the message
		// was handled correctly
		Message msg1 = assembler.pollMessageQueue();
		Message msg2 = assembler.pollMessageQueue();
		
		Assert.assertEquals(msg1.getId(), "0");
		Assert.assertArrayEquals("岡ト毎援じ覧計 \r\n".getBytes(), msg1.getContent());
		
		Assert.assertEquals(msg2.getId(), "0");
		Assert.assertArrayEquals("aaaaabbbb\r\n".getBytes(), msg2.getContent());
	}
	
	/*
	 * Test whether partial messages work
	 */
	@Test
	public void testPartialWrite() throws IOException {
		// Set up channel and connect it to the server
		IRCMessageAssembler assembler = new IRCMessageAssembler();
		SocketChannel channel = SocketChannel.open();
		channel.connect(new InetSocketAddress("localhost", server.getPortNumber()));
		channel.configureBlocking(true);
		
		// Write a message to the echo server
		ByteBuffer newBuffer = ByteBuffer.allocate(1024);
		ByteBuffer buf = ByteBuffer.allocate(1024);
		newBuffer.put("EXAMPLE MESSA".getBytes());
		newBuffer.flip();
		channel.write(newBuffer);
		newBuffer.clear();
		
		assembler.readMessage("0", channel, buf);
		
		newBuffer.put("GE \n\r\n".getBytes());
		newBuffer.flip();
		channel.write(newBuffer);
		assembler.readMessage("0", channel, buf);
		
		// Read from the assembler and test whether the message
		// was handled correctly
		Message msg = assembler.pollMessageQueue();
		
		Assert.assertEquals(msg.getId(), "0");
		Assert.assertArrayEquals(msg.getContent(), "EXAMPLE MESSAGE \n\r\n".getBytes());
	}
	
	/*
	 * Test whether multiple partial messages work
	 */
	@Test
	public void testPartialWrites() throws IOException {
		// Set up channel and connect it to the server
		IRCMessageAssembler assembler = new IRCMessageAssembler();
		SocketChannel channel1 = SocketChannel.open();
		SocketChannel channel2 = SocketChannel.open();
		channel1.connect(new InetSocketAddress("localhost", server.getPortNumber()));
		channel2.connect(new InetSocketAddress("localhost", server.getPortNumber()));
		
		// Write a message to the echo server
		ByteBuffer newBuffer = ByteBuffer.allocate(1024);
		ByteBuffer buf = ByteBuffer.allocate(1024);
		newBuffer.put("EXAMPLE MESSA".getBytes());
		newBuffer.flip();
		channel1.write(newBuffer);
		newBuffer.flip();
		int written = channel2.write(newBuffer);
		System.out.println("Written: " + written);
		newBuffer.clear();
		
		assembler.readMessage("0", channel1, buf);
		assembler.readMessage("1", channel2, buf);
		
		newBuffer.put("GE \n\r\n".getBytes());
		newBuffer.flip();
		channel1.write(newBuffer);
		newBuffer.flip();
		channel2.write(newBuffer);
		assembler.readMessage("0", channel1, buf);
		assembler.readMessage("1", channel2, buf);
		
		// Read from the assembler and test whether the message
		// was handled correctly
		Message msg1 = assembler.pollMessageQueue();
		
		Message msg2 = assembler.pollMessageQueue();
		
		Assert.assertArrayEquals(msg1.getContent(), "EXAMPLE MESSAGE \n\r\n".getBytes());
		Assert.assertArrayEquals(msg2.getContent(), "EXAMPLE MESSAGE \n\r\n".getBytes());
	}
}
