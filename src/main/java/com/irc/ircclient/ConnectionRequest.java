package com.irc.ircclient;

/*
 * Simple class requesting connection requests
 */
public class ConnectionRequest {
	private String id;
	private String hostname;
	private int port;
	

	public ConnectionRequest(String id, String hostname, int port) {
		super();
		this.id = id;
		this.hostname = hostname;
		this.port = port;
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
}
