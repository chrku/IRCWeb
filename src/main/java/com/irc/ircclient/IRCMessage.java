package com.irc.ircclient;

import java.util.ArrayList;

/*
 * Container class for IRC messages
 */
public class IRCMessage {
	
	private IRCMessageType type;
	private ArrayList<String> args;
	
	public IRCMessage(IRCMessageType type, ArrayList<String> args2) {
		super();
		this.type = type;
		this.args = args2;
	}

	public IRCMessageType getType() {
		return type;
	}

	public void setType(IRCMessageType type) {
		this.type = type;
	}

	public ArrayList<String> getArgs() {
		return args;
	}

	public void setArgs(ArrayList<String> args) {
		this.args = args;
	}
}
