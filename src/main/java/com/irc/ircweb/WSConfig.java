package com.irc.ircweb;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WSConfig implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		try {
			registry.addHandler(getHandler(), "/ws");
		} catch (IOException e) {
			System.exit(-1);
		}
		
	}
	
	@Bean
	public WebSocketHandler getHandler() throws IOException {
		return new WSHandler();
	}

}
