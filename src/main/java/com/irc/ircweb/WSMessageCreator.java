package com.irc.ircweb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/*
 * Creates messages to communicate with clients
 * JSON is used as a message form
 */
public class WSMessageCreator {
	
	JsonFactory factory;
	
	WSMessageCreator() {
		factory = new JsonFactory();
	}
	
	// Message type constants
	private final String typeString = "type";
	
	public TextMessage generateSuccess() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonGenerator gen = factory.createGenerator(out);
		
		// Write the message
		gen.writeStartObject();
		gen.writeStringField(typeString, "WS-CONNECTION-SUCCESS");
		gen.writeEndObject();
		gen.close();
		
		
		return new TextMessage(out.toByteArray());
	}

	public TextMessage generatePong() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonGenerator gen = factory.createGenerator(out);
		
		// Write the message
		gen.writeStartObject();
		gen.writeStringField(typeString, "PONG");
		gen.writeEndObject();
		gen.close();
		
		
		return new TextMessage(out.toByteArray());
	}
			
}
