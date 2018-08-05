package com.irc.ircweb;

import org.springframework.web.socket.TextMessage;

public class WSMessageCreator {
	
	public TextMessage generateSuccess() {
		return new TextMessage("CONNECTION ESTABLISHED".getBytes());
	}
			
}
