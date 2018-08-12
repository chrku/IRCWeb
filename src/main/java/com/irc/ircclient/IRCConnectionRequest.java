package com.irc.ircclient;

/*
 * A class that represents a connection request
 * to an IRC server 
 */
public class IRCConnectionRequest {
	
	// General socket information
	private String serverHostname;
	private int port;
	
	public String getServerHostname() {
		return serverHostname;
	}


	public void setServerHostname(String serverHostname) {
		this.serverHostname = serverHostname;
	}


	public int getPort() {
		return port;
	}

	public IRCConnectionRequest(String serverHostname, int port, String nickname, String username, String hostname,
			String servername, String realname) {
		this.serverHostname = serverHostname;
		this.port = port;
	}
}
