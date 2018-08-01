package com.irc.ircweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/*
 * This will serve the static content
 * As this is a SPA, only one HTML file
 * is served
 */
@Controller
@SpringBootApplication
public class IrcwebApplication {

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		return "index.html";
	}
	
	public static void main(String[] args) {
		SpringApplication.run(IrcwebApplication.class, args);
	}
}
