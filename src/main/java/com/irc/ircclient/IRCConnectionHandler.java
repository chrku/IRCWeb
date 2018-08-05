package com.irc.ircclient;


/*
 * Worker thread for handling IRC connections
 */
public class IRCConnectionHandler extends Thread {

	private boolean running = true;
	
	public IRCConnectionHandler() {
		
	}
	
	@Override
	public void run() {
		while (isRunning()) {
		}
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}
}
