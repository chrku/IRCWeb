package com.irc.ircclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * Simple server to test basic network I/O
 */
public class SimpleEchoServer extends Thread {
	
	private class EchoHandler extends Thread {

		Socket connection;
		private DataOutputStream msgOut;
		private DataInputStream msgIn;
		
		private byte buffer[];
		final int bufferSize = 512;
		
		private boolean isRunning = true;
		
		public void setRunning(boolean isRunning) {
			this.isRunning = isRunning;
		}

		public EchoHandler(Socket conn) throws IOException {
			this.connection = conn;
			
			buffer = new byte[bufferSize];
			msgOut = new DataOutputStream(conn.getOutputStream());
			msgIn = new DataInputStream(conn.getInputStream());
		}
		
		@Override
		public void run() {
			
			// Receive a connection and echo whatever it sends
			while (isRunning) {
				try {
					int read = msgIn.read(buffer, 0, bufferSize);
					System.out.println(new String(buffer));
					msgOut.write(buffer, 0, read);
				} catch (IOException e) {
					isRunning = false;
					System.out.println("[ECHO SERVER]: Error on port" + connection.getLocalPort());
				}
			}
			
			try {
				connection.close();
			} catch (IOException e) {
				System.out.println("[ECHO SERVER]: Error on port" + connection.getLocalPort());
			}
		}
		
	}
	
	// Server socket and port number
	private ServerSocket server;
	// Collection of all connections that are associated with that server
	private ArrayList<EchoHandler> connectionPool;
	
    private boolean isRunning = true;
	
	public SimpleEchoServer(int portNumber) throws IOException {
		server = new ServerSocket(portNumber);
		connectionPool = new ArrayList<EchoHandler>();
;		
	}

	public int getPortNumber() {
		return server.getLocalPort();
	}

	public void run() {
		// Accept connections until isRunning is false
		while (isRunning) {
			try {
				Socket newConnection = server.accept();
				EchoHandler handler = new EchoHandler(newConnection);
				connectionPool.add(handler);
				handler.start();
			} catch (IOException e) {
				isRunning = false;
				System.out.println("[ECHO SERVER]: Error while accepting");
			}
		}
		for (EchoHandler handler : connectionPool) {
			handler.setRunning(false);
		}
		for (EchoHandler handler : connectionPool) {
			try {
				handler.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			server.close();
		} catch (IOException e) {
			System.out.println("[ECHO SERVER]: Error while closing");
		}
	}

	public void setRunning(boolean b) {
		isRunning = b;
	}
	
}
