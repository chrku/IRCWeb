package com.irc.ircclient;

public enum ClientMessageType {
	PASSWORD, NICKNAME, USER, OPER,
	QUIT, JOIN, PART, MODE, TOPIC, NAMES,
	LIST, INVITE, KICK, VERSION, STATS, LINK,
	TIME, CONNECT, TRACE, ADMIN, INFO, PRIVMSG, NOTICE,
	WHO, WHOIS, WHOWAS, PING, PONG, AWAY, USERS, UNKNOWN,
	
	// Server responses
	RPL_ENDOFMOTD
}
