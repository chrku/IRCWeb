package com.irc.ircclient;

import java.util.ArrayList;

/*
 * Container class for IRC messages
 */
public class IRCMessage {
	
	private String type;
	private ArrayList<String> args;
	
	public IRCMessage(String type, ArrayList<String> args2) {
		this.type = type;
		this.args = args2;
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
