package com.irc.ircclient;

public class Message {

	private String id;
	private byte[] content;
	
	public Message(String id, byte[] content) {
		super();
		this.id = id;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}
	
}
