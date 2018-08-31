
// WS socket
let socket = null;
let realname = null;
let nick = null;

// Unique id for each connection
let IDCounter = 0;

// Get the url for the WS protocol
function getWSURL() {
    let ws = "";
    if (window.location.protocol === "https:") {
        ws += "wss://";
    }
    else {
        ws += "ws://";
    }
    ws += window.location.host + "/ws";
    return ws;
}


// UI utility functions
function showLogin() {
	document.getElementById("login").style.display = "flex";
}

function hideLogin() {
	document.getElementById("login").style.display = "none";
}

function showSpinner() {
	document.getElementById("spin").style.display = "";
}

function hideSpinner() {
	document.getElementById("spin").style.display = "none";
}

function showFailure() {
	document.getElementById("failure").style.display = "block";
}

function hideFailure() {
	document.getElementById("failure").style.display = "none";
}

function showLoginError() {
	document.getElementById("login-error").style.display = "";
}

function hideLoginError() {
	document.getElementById("login-error").style.display = "none";
}

function showChatArea() {
	document.getElementById("chat-page").style.display = "flex";
}

function hideChatArea() {
	document.getElementById("chat-page").style.display = "none";
}

// UI state functions
function showLoginPage() {
	hideSpinner();
	hideFailure();
	hideChatArea();
	showLogin();
}

function showLoadPage() {
	hideLogin();
	hideFailure();
	hideChatArea();
	showSpinner();
}

function showFailurePage() {
	hideLogin();
	hideFailure();
	hideChatArea();
	showFailure();
}

function showChatPage() {
	hideLogin();
	hideFailure();
	hideSpinner();
	showChatArea();
}

// Functions for displaying messages
function createDefaultChatNode(sender, text) {
	
	// Create a new node to be displayed
	let node = document.createElement("div");

	// Sub-nodes
	let nick_sender = document.createElement("span");
	let chat_text = document.createElement("span");

	
	// Formatted text for sender + content
	let nick_sender_text = document.createTextNode(sender + ": ");
	let chat_text_text = document.createTextNode(text);
	nick_sender.classList.add("sender");
	chat_text.classList.add("text-chat");
	node.classList.add("chat-node");
	
	nick_sender.appendChild(nick_sender_text);
	chat_text.appendChild(chat_text_text);
	
	node.appendChild(nick_sender);
	node.appendChild(chat_text);
	
	return node;
}

function createTextNode(text) {
	// Create a new node to be displayed
	let node = document.createElement("div");

	let chat_text = document.createElement("span");
	let chat_text_text = document.createTextNode(text);
	chat_text.classList.add("text-chat");
	node.classList.add("chat-node");
	
	chat_text.appendChild(chat_text_text);
	
	node.appendChild(chat_text);
	
	return node;
}

function appendToMainChatWindow(node) {
	document.getElementById("chat-area").appendChild(node);
}

//Handle IRC messages
function handleMessage(message) {
	switch(message.type) {
	case "NOTICE":
		// Create chat node and append it to the chat window
		appendToMainChatWindow(createDefaultChatNode(message.args[1], message.args[2]));
		break;
	// These are all greeting messages
	// End of MotD/No MotD will be handed separately
	case "001": case "002": case "003": case "042": case "372": case "374": case "375":
	case "376":
		appendToMainChatWindow(createTextNode(message.args[1]));
	case "004": 
		break;
	default:
		break;
	}
}

// Setup the initial handshake with the server,
// i.e. transmit the USER and NICK messages
// according to RFC 1459
function setupInitialHandshake(socket) {
	let nick_irc_message = "NICK " + nick + "\r\n";
	let nick_json = {type: "SEND-IRC-MESSAGE", message: nick_irc_message};
	let user_irc_message = "USER webirc webirc webirc :" + realname + "\r\n";
	let user_json = {type: "SEND-IRC-MESSAGE", message: user_irc_message};
	socket.send(JSON.stringify(nick_json));
	socket.send(JSON.stringify(user_json));
}

// Attempt to connect to WS server
function attemptConnection() {

	let url = getWSURL();
    // We use a custom protocol
    let socket = new WebSocket(url);
    
    socket.id = IDCounter;
    socket.IRCConnectionEstablished = false;
    socket.showChatWindow = false;
    socket.initialHandshakeCompleted = false;
    
    IDCounter += 1;
    
    socket.onmessage = function(event) {
    	console.log(event.data);
    	// Message format is JSON
    	let response = JSON.parse(event.data);
    	switch (response.type) {
    	case "WS-CONNECTION-SUCCESS":
    		showLoginPage();
    		break;
    	case "NEW-MESSAGES":
    		if (this.IRCConnectionEstablished == false) {
    			this.IRCConnectionEstablished = true;
    		}
    		response.args.forEach(handleMessage);
    		break;
    	case "NO-NEW-MESSAGES":
    		if (this.IRCConnectionEstablished == false) {
    			this.IRCConnectionEstablished = true;
    		}
    		break;
    	case "FAILURE-CONNECTION-ERROR":
        	showFailurePage();
    		break;
    	}
    	if (this.IRCConnectionEstablished && this.showChatWindow == false) {
    		this.showChatWindow = true;
    		showChatPage();
    		setupInitialHandshake(this);
    	}
    }
    
    socket.onclose = function() {
    	showFailurePage();
    }
    
    socket.onerror = function() {
    	showFailurePage();
    }

    return socket;
}

function connect() {
	
	// Get the values of the input fields
	let hostname = document.getElementById("hostname").value;
	let port = document.getElementById("port").value;
	// Nick and hostname are global since they are needed later
	nick = document.getElementById("nick").value;
	realname = document.getElementById("realname").value;
	if (hostname.length === 0 || port.length === 0 || nick.length === 0) {
		showLoginError();
		return;
	}
	
	hideLoginError();
	showLoadPage();
	
	// Assemble object from form values
	let server_query = {type: "CONNECTION-ATTEMPT", "hostname": hostname, "port": port};
	let ws_query = JSON.stringify(server_query);
	socket.send(ws_query);
	
	setInterval(function() {
		checkMessages();
	}, 1000);
}

function checkMessages() {
	let message_query = {type : "READ-MESSAGES"};
	let ws_query = JSON.stringify(message_query);
	socket.send(ws_query);
}

/*
 * This will fire once the DOM is initialized
 */
document.addEventListener("DOMContentLoaded", function() {
	  showLoadPage();
	  socket = attemptConnection();
});
