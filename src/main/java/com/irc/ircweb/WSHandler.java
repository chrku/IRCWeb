package com.irc.ircweb;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.irc.ircclient.IRCConnectionHandler;

import java.io.IOException;

import org.springframework.web.socket.TextMessage;

public class WSHandler extends TextWebSocketHandler {
	
	private ConnectionRegistry registry;
	private WSMessageCreator creator;
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage msg) {
		
		// Get session id
		String id = session.getId();
		IRCConnectionHandler handler = registry.getHandler(id);
		if (handler != null) {
			
		}
		else {
			try {
				session.sendMessage(creator.generateNotConnected());
			} catch (IOException e) {
				System.out.println("[ERROR] Sending WS message");
				e.printStackTrace();
			}
		}
	}
}
