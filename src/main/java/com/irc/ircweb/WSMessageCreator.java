package com.irc.ircweb;

import org.springframework.web.socket.TextMessage;

public class WSMessageCreator {
	
	public TextMessage generateNotConnected() {
		return new TextMessage("NOT CONNECTED".getBytes());
	}
			
}
