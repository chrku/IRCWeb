
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

function showChannels(socket) {
	// RFC 1459 4.2.6, this lists all channels
	let ws_query = {
			type: "SEND-IRC-MESSAGE",
			message: "LIST\r\n"
	};
	socket.send(JSON.stringify(ws_query));
}

function sendToNetwork(event) {
	// Parse messages; messages beginning with / indicate that the user
	// wants to send the first argument as a command
	let sendString = this.UINode.firstElementChild.lastElementChild.value;
	
	
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
    UINode.mainChatPage.firstElementChild.lastElementChild.addEventListener("submit", sendToNetwork.bind(socket));

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

// This makes sure IDs are unique
function applyPreOrderDOMTreeID(ID, root) {
	root.id = root.id + ID;
	for (let i = 0; i < root.children.length; ++i) {
		applyPreOrderDOMTreeID(ID, root.children[i]);
	}
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
	applyPreOrderDOMTreeID(IDCounter, loginNode);
	loginNode.style.display = "flex";
	let failureNodeWS = document.getElementById("failure-page-ws").cloneNode(true);
	applyPreOrderDOMTreeID(IDCounter, failureNodeWS);
	failureNodeWS.style.display = "block";
	let failureNodeIRC = document.getElementById("failure-page-irc").cloneNode(true);
	applyPreOrderDOMTreeID(IDCounter, failureNodeIRC);
	failureNodeIRC.style.display = "block";
	let loadingNode = document.getElementById("loading-page").cloneNode(true);
	applyPreOrderDOMTreeID(IDCounter, loadingNode);
	loadingNode.style.display = "block";
	let mainChatNode = document.getElementById("chat-page").cloneNode(true);
	applyPreOrderDOMTreeID(IDCounter, mainChatNode);
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
	let hours = currentTime.getHours().toString();
	let minutes = currentTime.getMinutes().toString();
	let dateString = "[" + hours.padStart(2, "0") + ":" + minutes.padStart(2, "0") + "]";
	
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

function appendToChatWindow(UINode, nick, content, error="") {
	UINode.mainChatPage.firstElementChild.firstElementChild.appendChild(createMessageNode(nick, content, error));
}

//Handle IRC messages
function handleMessage(message) {
	switch(message.type) {
	
	case "NOTICE":
		appendToChatWindow(this.UINode, message.sender, message.trailer);
		break;
	// Greeting messages
	case "001": case "002": case "003": case "251": case "255": case "265":
	case "266":
		appendToChatWindow(this.UINode, "GREETING", message.trailer);
		break;
	case "004":
		appendToChatWindow(this.UINode, "SERVER", message.args[0] + " " + message.args[1] + " " + message.args[2] + " " + message.args[3]);
		break;
	// RPL_ISUPPORT messages
	// Might be used later, for now just display it
	case "005":
		appendToChatWindow(this.UINode, "SERVER", message.args.reduce((acc, c) => acc + " " + c.toString()));
		break;
	// Misc. messages (unique ID, num channels...)
	case "254": case "042":
		appendToChatWindow(this.UINode, "SERVER", message.args[1] + " " + message.trailer);
		break;
	// Channel messages
	case "321":
		appendToChatWindow(this.UINode, "SERVER", message.trailer);
		break;
	case "322":
		appendToChatWindow(this.UINode, "SERVER", message.args[0] + " " + message.args[1] + " " + message.trailer);
		break;
	case "323":
		appendToChatWindow(this.UINode, "SERVER", message.trailer);
		break;
	// Once we get MOTD we can query users and channels
	case "375":
		appendToChatWindow(this.UINode, "MOTD", message.trailer);
		showChannels(this);
		break;
	case "372": case "376":
		appendToChatWindow(this.UINode, "MOTD", message.trailer);
		break;
	default:
		appendToChatWindow(this.UINode, "SERVER", message.trailer, message.type);
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
