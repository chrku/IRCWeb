package com.irc.ircclient;

import java.util.ArrayList;

/*
 * Container class for IRC messages
 */
public class IRCMessage {
	
	private String type;
	private ArrayList<String> args;
	private String trailer;
	private String sender;
	
	public IRCMessage() {
	}
	
	public IRCMessage(String type, ArrayList<String> args2, String trailer, String sender) {
		this.type = type;
		this.args = args2;
		this.trailer = trailer;
		this.sender = sender;
	}
	
	public String getTrailer() {
		return trailer;
	}

	public void setTrailer(String trailer) {
		this.trailer = trailer;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<String> getArgs() {
		return args;
	}

	public void setArgs(ArrayList<String> args) {
		this.args = args;
	}
}
