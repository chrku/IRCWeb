package com.irc.ircweb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.web.socket.TextMessage;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irc.ircclient.IRCMessage;

/*
 * Creates messages to communicate with clients
 * JSON is used as a message form
 */
public class WSMessageCreator {
	
	private JsonFactory factory;
	private ObjectMapper mapper;
	
	WSMessageCreator() {
		factory = new JsonFactory();
		mapper = new ObjectMapper();

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
	
	public TextMessage generateConnectionPending() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonGenerator gen = factory.createGenerator(out);
		
		// Write the message
		gen.writeStartObject();
		gen.writeStringField(typeString, "FAILURE-CONNECTION-PENDING");
		gen.writeEndObject();
		gen.close();
		
		
		return new TextMessage(out.toByteArray());
	}
	
	public TextMessage generateConnectionError() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonGenerator gen = factory.createGenerator(out);
		
		// Write the message
		gen.writeStartObject();
		gen.writeStringField(typeString, "FAILURE-CONNECTION-ERROR");
		gen.writeEndObject();
		gen.close();
		
		
		return new TextMessage(out.toByteArray());
	}
	
	public TextMessage noNewMessages() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonGenerator gen = factory.createGenerator(out);
		
		// Write the message
		gen.writeStartObject();
		gen.writeStringField(typeString, "NO-NEW-MESSAGES");
		gen.writeEndObject();
		gen.close();
		
		
		return new TextMessage(out.toByteArray());
	}
	
	public TextMessage newMessages(ArrayList<IRCMessage> messages) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonGenerator gen = factory.createGenerator(out);
		gen.setCodec(mapper);

		// Write the message
		gen.writeStartObject();
		gen.writeStringField(typeString, "NEW-MESSAGES");
		gen.writeArrayFieldStart("args");
		for (IRCMessage msg : messages) {
			gen.writeObject(msg);
		}
		gen.writeEndArray();
		gen.writeEndObject();
		gen.close();
		
		return new TextMessage(out.toByteArray());
	}
}
