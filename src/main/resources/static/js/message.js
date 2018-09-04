
// WS socket
let socket = null;
let realname = null;
let nick = null;

// Unique id for each connection
let IDCounter = 0;

// UI state management
let pages = {};
let currentId = 0;

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

// Update UI state
function updateUIState() {
	let rootNode = document.getElementById("server-container");
	rootNode.firstElementChild.remove();
	rootNode.appendChild(pages[currentId].activePage);
}

function respondToMessage(event) {
	console.log(event.data);
	// Message format is JSON
	let response = JSON.parse(event.data);
	switch (response.type) {
	case "WS-CONNECTION-SUCCESS":
		this.UINode.activePage = this.UINode.loginPage;
		updateUIState();
		break;
	case "NEW-MESSAGES":
		if (this.IRCConnectionEstablished == false) {
			this.IRCConnectionEstablished = true;
		}
		response.args.forEach(handleMessage.bind(this));
		break;
	case "NO-NEW-MESSAGES":
		if (this.IRCConnectionEstablished == false) {
			this.IRCConnectionEstablished = true;
		}
		break;
	case "FAILURE-CONNECTION-ERROR":
		this.UINode.activePage = this.UINode.failurePageIRC;
		updateUIState();
		break;
	}
	if (this.IRCConnectionEstablished && this.showChatWindow == false) {
		this.showChatWindow = true;
		this.UINode.activePage = this.UINode.mainChatPage;
		updateUIState();
		setupInitialHandshake(this);
	}
}

function respondToFailure(socket) {
	this.UINode.activePage = this.UINode.failurePageWS;
	updateUIState();
}

//Attempt to connect to WS server
function attemptConnection(UINode) {

	// Create web socket
	let url = getWSURL();
    let socket = new WebSocket(url);
    socket.UINode = UINode;
    
    // Set up callbacks
    socket.onmessage = respondToMessage.bind(socket);
    socket.onclose = respondToFailure.bind(socket);
    socket.onerror = respondToFailure.bind(socket);
    
    // Set up UI related information
    socket.showChatWindow = false;
    socket.IRCConnectionEstablished = false;
    
    // Set up socket related callbacks
    UINode.loginPage.firstElementChild.lastElementChild.addEventListener("click", connectToIRCNetwork.bind(socket));

    return socket;
}

function displayServer(event) {
	currentId = this.id;
	updateUIState();
}

function showRequiredFields(loginNode) {
	loginNode.firstElementChild.children[9].removeAttribute("hidden");
}

function hideRequiredFields(loginNode) {
	loginNode.firstElementChild.children[9].createAttribute("hidden");
}

function connectToIRCNetwork() {
	// Get the values of the input fields
	let hostname = this.UINode.loginPage.firstElementChild.children[1].value;
	let port = this.UINode.loginPage.firstElementChild.children[3].value;
	// Nick and hostname are global since they are needed later
	let nick = this.UINode.loginPage.firstElementChild.children[5].value;
	let realname = this.UINode.loginPage.firstElementChild.children[7].value;
	this.nick = nick;
	this.realname = realname;
	
	
	if (hostname.length === 0 || port.length === 0 || nick.length === 0) {
		showRequiredFields(this.UINode.loginPage);
		return;
	}
	
	// Assemble object from form values
	let server_query = {type: "CONNECTION-ATTEMPT", "hostname": hostname, "port": port};
	let ws_query = JSON.stringify(server_query);
	this.send(ws_query);
	
	this.UINode.activePage = this.UINode.loadingPage;
	updateUIState();
	
	setInterval(checkMessages.bind(this), 1000);
}

// Add a server element to the server list
function addServer() {
	
	// Add a new server to the list
	let serverListNode = document.getElementById("serverlist");
	let serverNode = document.createElement("div");
	let serverInitialText = document.createTextNode("New Server " + IDCounter);
	serverNode.appendChild(serverInitialText);
	serverNode.setAttribute("id", IDCounter);
	serverNode.addEventListener("click", displayServer)
	serverListNode.appendChild(serverNode);
	
	// Clone UI state and make pages visible
	let loginNode = document.getElementById("login-page").cloneNode(true);
	loginNode.style.display = "flex";
	let failureNodeWS = document.getElementById("failure-page-ws").cloneNode(true);
	failureNodeWS.style.display = "block";
	let failureNodeIRC = document.getElementById("failure-page-irc").cloneNode(true);
	failureNodeIRC.style.display = "block";
	let loadingNode = document.getElementById("loading-page").cloneNode(true);
	loadingNode.style.display = "block";
	let mainChatNode = document.getElementById("chat-page").cloneNode(true);
	mainChatNode.style.display = "flex";
	
	// Create new server state object
	let UINode = {
			// Active page: Currently displayed page for this server
			activePage: loadingNode,
			// Login page: DOM node representing the login page
			loginPage: loginNode,
			// Failure page representing failure in WS
			failurePageWS: failureNodeWS,
			// ... and IRC
			failurePageIRC: failureNodeIRC,
			// Loading spinner
			loadingPage: loadingNode,
			// Main chat window
			mainChatPage: mainChatNode
	}
	
	pages[IDCounter] = UINode;
	
	IDCounter += 1;
	// Create new connection to central server
	attemptConnection(UINode);
}

function createMessageNode(nick, content, error = "") {
	let messageNode = document.createElement("div");
	
	let timeNode = document.createElement("span");
	let nickNode = document.createElement("span");
	let contentNode = document.createElement("span");
	let errorNode = document.createElement("span");
	
	timeNode.classList.add("message-time");
	nickNode.classList.add("message-nick");
	contentNode.classList.add("message-content");
	errorNode.classList.add("message-error");
	
	// Get current time (hr/min)
	let currentTime = new Date();
	let dateString = "[" + currentTime.getHours() + ":" + currentTime.getMinutes() + "]";
	
	// Pad nick field
	// Maximum nick length according to RFC 1459 is 9
	// Error codes are 3 digits, no error gets padded
	let nickString = nick.padStart(9);
	let errorString = error.padStart(3);
	
	let timeTextNode = document.createTextNode(dateString);
	let nickTextNode = document.createTextNode(nickString);
	let contentTextNode = document.createTextNode(content);
	let errorTextNode = document.createTextNode(errorString);
	
	timeNode.appendChild(timeTextNode);
	nickNode.appendChild(nickTextNode);
	contentNode.appendChild(contentTextNode);
	errorNode.appendChild(errorTextNode);

	messageNode.appendChild(timeNode);
	messageNode.appendChild(nickNode);
	messageNode.appendChild(errorNode);
	messageNode.appendChild(contentNode);

	return messageNode;
}

//Handle IRC messages
function handleMessage(message) {
	switch(message.type) {
	case "NOTICE":
		// Create chat node and append it to the chat window
		this.UINode.mainChatPage.firstElementChild.firstElementChild.appendChild(createMessageNode(message.args[0], message.trailer));
		break;
	// These are all greeting messages
	// End of MotD/No MotD will be handed separately
	case "001": case "002": case "003": case "042": case "372": case "374": case "375":
	case "376":
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

function checkMessages() {
	let message_query = {type : "READ-MESSAGES"};
	let ws_query = JSON.stringify(message_query);
	this.send(ws_query);
}
