
// WS socket
let socket = null;
let realname = null;
let nick = null;

// Chat state
let connectedToIRCServer = false;

let messages = [];

const PING_QUERY = {type: "PING"};

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
function showLoginPage() {
	document.getElementById("login").style.display = "flex";
}

function hideLoginPage() {
	document.getElementById("login").style.display = "none";
}

function showSpinner() {
	document.getElementById("spin").style.display = "";
}

function hideSpinner() {
	document.getElementById("spin").style.display = "none";
}

function showFailure() {
	document.getElementById("failure").style.display = "";
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
	default:
		break;
	}
}

// Attempt to connect to WS server
function attemptConnection() {

	let url = getWSURL();
    // We use a custom protocol
    let socket = new WebSocket(url);
    
    socket.onmessage = function(event) {
    	console.log(event.data);
    	// Message format is JSON
    	let response = JSON.parse(event.data);
    	switch (response.type) {
    	case "WS-CONNECTION-SUCCESS":
    		hideSpinner();
    		showLoginPage();
    		break;
    	case "PONG":
    		break;
    	case "NEW-MESSAGES":
    		if (!connectedToIRCServer)
    			connectedToIRCServer = true;
    		response.args.forEach(handleMessage);
    		break;
    	case "NO-NEW-MESSAGES":
    		if (!connectedToIRCServer)
    			connectedToIRCServer = true;
    		break;
    	}
    	if (connectedToIRCServer) {
    		hideSpinner();
    		showChatArea();
    	}
    }
    
    socket.onclose = function() {
    	hideSpinner();
    	hideLoginPage();
    	showFailure();
	}
    
    socket.onerror = function() {
    	hideSpinner();
    	hideLoginPage();
    	showFailure();
    }

    return socket;
}

function connect() {
	/*
	 * Get the values of the input fields
	 */
	let hostname = document.getElementById("hostname").value;
	let port = document.getElementById("port").value;
	nick = document.getElementById("nick").value;
	realname = document.getElementById("realname").value;
	if (hostname.length === 0 || port.length === 0 || nick.length === 0) {
		showLoginError();
		return;
	}
	showSpinner();
	hideLoginPage();
	hideLoginError();
	
	/*
	 * Assemble object from form values
	 */
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
	  showSpinner();
	  socket = attemptConnection();
});