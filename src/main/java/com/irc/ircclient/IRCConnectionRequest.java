package com.irc.ircclient;

/*
 * A class that represents a connection request
 * to an IRC server 
 */
public class IRCConnectionRequest {
	
	// General socket information
	private String serverHostname;
	private int port;
	
	// IRC-specific information for registration
	private String nickname;
	private String username;
	private String hostname;

	private String servername;
	private String realname;
	
	
	public String getServerHostname() {
		return serverHostname;
	}


	public void setServerHostname(String serverHostname) {
		this.serverHostname = serverHostname;
	}


	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	public String getNickname() {
		return nickname;
	}


	public void setNickname(String nickname) {
		this.nickname = nickname;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getHostname() {
		return hostname;
	}


	public void setHostname(String hostname) {
		this.hostname = hostname;
	}


	public String getServername() {
		return servername;
	}


	public void setServername(String servername) {
		this.servername = servername;
	}


	public String getRealname() {
		return realname;
	}


	public void setRealname(String realname) {
		this.realname = realname;
	}


	public IRCConnectionRequest(String serverHostname, int port, String nickname, String username, String hostname,
			String servername, String realname) {
		this.serverHostname = serverHostname;
		this.port = port;
		this.nickname = nickname;
		this.username = username;
		this.hostname = hostname;
		this.servername = servername;
		this.realname = realname;
	}
}
