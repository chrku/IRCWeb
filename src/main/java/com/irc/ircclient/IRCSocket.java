package com.irc.ircclient;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

// IRC client class, using TCP sockets

public class IRCSocket {
	
	// Socket used to connect with the IRC server
	private Socket connectorSocket;
	
	private DataOutputStream msgOut;
	private DataInputStream msgIn;
	
	private byte buffer[];
	final int bufferSize = 2048;
	
	// Input and output streams of the socket
	
	// Server host name and port number
	private String hostName;
	private int portNumber;
	
	
	/*
	 * Construct an IRC client which can be used to send and receive messages with a given
	 * server
	 * 
	 * @param hostName server hostname
	 * @param portNumber server port number
	 * 
	 */
	public IRCSocket(String hostName, int portNumber) throws IOException {
		this.hostName = hostName;
		this.portNumber = portNumber;
		
		connectorSocket = new Socket(hostName, portNumber);
		msgIn = new DataInputStream(connectorSocket.getInputStream());
		msgOut = new DataOutputStream(connectorSocket.getOutputStream());
		
		buffer = new byte[bufferSize];
	}
	
	/*
	 * Attempt to connect to an IRC server
	 * 
	 */
	public void connect() {
		
	}
	
	public void close() throws IOException {
		connectorSocket.close();
	}
	
	/*
	 * Send a message to the server
	 */
	public void sendMsg(byte[] msg) throws IOException {
		msgOut.write(msg);
	}
	
	/*
	 * Receive a message into the local buffer  
	 */
	public int recvMsg() throws IOException {
		return msgIn.read(buffer, 0, bufferSize);
	}

	public String getHostName() {
		return hostName;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public byte[] getBuffer() {
		return buffer;
	}
	
}