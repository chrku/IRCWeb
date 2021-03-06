
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
	socket.close();
	updateUIState();
}

function showChannels(socket) {
	// RFC 1459 4.2.6, this lists all channels
	let wsQuery = {
			type: "SEND-IRC-MESSAGE",
			message: "LIST\r\n"
	};
	socket.send(JSON.stringify(wsQuery));
}

function sendToNetwork(event) {
	// Parse messages; messages beginning with / indicate that the user
	// wants to send the first argument as a command
	if (event.key == "Enter") {
		let sendString = this.UINode.mainChatPage.firstElementChild.lastElementChild.value;
		this.UINode.mainChatPage.firstElementChild.lastElementChild.value = "";
		sendString = sendString.trimStart();
		
		// Extract argument
		if (sendString[0] == '/') {
			let commandEnd = sendString.indexOf(' ');
			if (commandEnd == -1) {
				commandEnd = sendString.length;
			}
			let command = sendString.substr(1, commandEnd);
			let finalString = command + sendString.substr(commandEnd, sendString.length) + "\r\n";
			console.log(finalString);
			let wsQuery = {
					type: "SEND-IRC-MESSAGE",
					message: finalString
			}
			this.send(JSON.stringify(wsQuery));
		}
		else {
			appendToChatWindow(this.UINode, "CLIENT" , "You can only send commands (starting with '/') from here. Join a channel if you want to chat.");
		}
	}
}

function sendToNetworkChannel(name, event) {
	// Parse messages; messages beginning with / indicate that the user
	// wants to send the first argument as a command
	if (event.key == "Enter") {
		let sendString = this.UINode.channelWindows[name].firstElementChild.lastElementChild.value;
		this.UINode.channelWindows[name].firstElementChild.lastElementChild.value = "";
		sendString = sendString.trimStart();
		
		// Extract argument
		if (sendString[0] == '/') {
			let commandEnd = sendString.indexOf(' ');
			if (commandEnd == -1) {
				commandEnd = sendString.length;
			}
			let command = sendString.substr(1, commandEnd);
			let finalString = command + sendString.substr(commandEnd, sendString.length) + "\r\n";
			console.log(finalString);
			let wsQuery = {
					type: "SEND-IRC-MESSAGE",
					message: finalString
			}
			this.send(JSON.stringify(wsQuery));
		}
		else {
			let finalString = "PRIVMSG " + name + " :" + sendString + "\r\n";
			createChannelIfNotExistAndAppend(this.UINode, this.nick, sendString);
			let wsQuery = {
					type: "SEND-IRC-MESSAGE",
					message: finalString
			}
			console.log(finalString);
			this.send(JSON.stringify(wsQuery));
		}
	}
}

function sendToNetworkPrivate(name, event) {
	// Parse messages; messages beginning with / indicate that the user
	// wants to send the first argument as a command
	if (event.key == "Enter") {
		let sendString = this.UINode.privateMessageWindows[name].firstElementChild.lastElementChild.value;
		this.UINode.privateMessageWindows[name].firstElementChild.lastElementChild.value = "";
		sendString = sendString.trimStart();
		
		// Extract argument
		if (sendString[0] == '/') {
			let commandEnd = sendString.indexOf(' ');
			if (commandEnd == -1) {
				commandEnd = sendString.length;
			}
			let command = sendString.substr(1, commandEnd);
			let finalString = command + sendString.substr(commandEnd, sendString.length) + "\r\n";
			console.log(finalString);
			let wsQuery = {
					type: "SEND-IRC-MESSAGE",
					message: finalString
			}
			this.send(JSON.stringify(wsQuery));
		}
		else {
			let finalString = "PRIVMSG " + name + " :" + sendString + "\r\n";
			createPrivateIfNotExistAndAppend(this.UINode, this.nick, sendString);
			let wsQuery = {
					type: "SEND-IRC-MESSAGE",
					message: finalString
			}
			console.log(finalString);
			this.send(JSON.stringify(wsQuery));
		}
	}
}

//This makes sure IDs are unique
function applyPreOrderDOMTreeID(ID, root) {
	root.id = root.id + ID;
	for (let i = 0; i < root.children.length; ++i) {
		applyPreOrderDOMTreeID(ID, root.children[i]);
	}
}

function switchToChannelWindow(window, UINode) {
	currentId = UINode.ID;
	UINode.activePage = window;
	updateUIState();
}

function messageSelectedUser(channelNode, UINode) {
	let selectedUser = channelNode.selectedUser;
	if (selectedUser) {
		let name = selectedUser.name;
		addNewPrivateWindow(UINode, name);
	}
}

function addNewChannelWindow(UINode, channelName) {
	if (!UINode.channelWindows[channelName]) {
		// Add new channel window
		let channelWindowNode = document.getElementById("channel-page").cloneNode(true);
		channelWindowNode.style.display = "flex";
		channelWindowNode.firstElementChild.lastElementChild.addEventListener("keydown", function(event) {sendToNetworkChannel.call(UINode.socket, channelName, event);});
		channelWindowNode.lastElementChild.lastElementChild.addEventListener("click", () => messageSelectedUser(channelWindowNode, UINode));
		applyPreOrderDOMTreeID(channelName + UINode.ID, channelWindowNode);
		UINode.channelWindows[channelName] = channelWindowNode;
		
		// Add channel to list of subscribed channels
		let newChannelEntry = document.createElement("div");
		newChannelEntry.appendChild(document.createTextNode(channelName));
		newChannelEntry.channelName = channelName;
		newChannelEntry.addEventListener("click", () => switchToChannelWindow(channelWindowNode, UINode));
		UINode.channelListNode.appendChild(newChannelEntry);
		
		if (!UINode.switchToMainPage)
			UINode.switchToMainPage = true;
	}
}

function addNewPrivateWindow(UINode, name) {
	if (!UINode.privateMessageWindows[name]) {
		let privateWindowNode = document.getElementById("private-page").cloneNode(true);
		privateWindowNode.style.display = "flex";
		privateWindowNode.firstElementChild.lastElementChild.addEventListener("keydown", function(event) {sendToNetworkPrivate.call(UINode.socket, name, event);} );
		applyPreOrderDOMTreeID(name + UINode.ID, privateWindowNode);1
		UINode.privateMessageWindows[name] = privateWindowNode;
	
		// Add channel to list of subscribed channels
		let newChannelEntry = document.createElement("div");
		newChannelEntry.appendChild(document.createTextNode(name));
		newChannelEntry.channelName = name;
		newChannelEntry.addEventListener("click", () => switchToChannelWindow(privateWindowNode, UINode));
		UINode.channelListNode.appendChild(newChannelEntry);
		
		if (!UINode.switchToMainPage)
			UINode.switchToMainPage = true;
	}
}

function joinChannel() {
	let channelName = this.UINode.selectedChannel.channelName;
	// RFC 1459 JOIN command
	let wsQuery = {
			type: "SEND-IRC-MESSAGE",
			message: "JOIN " + channelName + "\r\n"
	};
	
	this.send(JSON.stringify(wsQuery));
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
    UINode.mainChatPage.firstElementChild.lastElementChild.addEventListener("keydown", sendToNetwork.bind(socket));
	// Initially disable channel join button and add join
	// channel command
    UINode.mainChatPage.lastElementChild.lastElementChild.disabled = true;
    UINode.mainChatPage.lastElementChild.lastElementChild.addEventListener("click", joinChannel.bind(socket));

    return socket;
}

function displayServer() {
	currentId = this.ID;
	if (this.switchToMainPage)
		this.activePage = this.mainChatPage;
	
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
	
	this.UINode.socket = this;
	
	setInterval(checkMessages.bind(this), 1000);
}

// Add a server element to the server list
function addServer() {
	
	// Add a new server to the list
	let serverListNode = document.getElementById("serverlist");
	let serverNode = document.createElement("div");
	let channelListNode = document.createElement("div");
	let serverInitialText = document.createTextNode("New Server " + IDCounter);
	serverNode.appendChild(serverInitialText);
	serverNode.classList.add("server-node");
	serverListNode.appendChild(serverNode);
	serverListNode.appendChild(channelListNode);
	
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
			mainChatPage: mainChatNode,
			// Selected channel in list
			selectedChannel: null,
			// Windows for individual channels and PMs
			channelWindows: {},
			// Subscribed channel list for this server
			channelListNode: channelListNode,
			
			ID: IDCounter,
			
			switchToMainPage: false,
			
			privateMessageWindows: {},
			
			socket: null
	}
	
	serverNode.addEventListener("click", displayServer.bind(UINode));
	
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

function appendToWindow(window, nick, content, error="") {
	window.firstElementChild.firstElementChild.appendChild(createMessageNode(nick, content, error));
}

function setSelectedChannel(UINode, channelNode) {
	if (UINode.selectedChannel)
		UINode.selectedChannel.style.backgroundColor = "";
	else 
		UINode.mainChatPage.lastElementChild.lastElementChild.disabled = false;
	UINode.selectedChannel = channelNode;
	channelNode.style.backgroundColor = "blue";
}

function setSelectedUser(channelWindow, nameElement) {
	if (channelWindow.selectedUser)
		channelWindow.selectedUser.style.backgroundColor = "";
	
	channelWindow.selectedUser = nameElement;
	nameElement.style.backgroundColor = "blue";
}

function appendToChannelWindow(UINode, channelName) {
	let channelNode = document.createElement("div");
	channelNode.appendChild(document.createTextNode(channelName));
	channelNode.channelName = channelName;
	channelNode.addEventListener("click", () => setSelectedChannel(UINode, channelNode));
	UINode.mainChatPage.lastElementChild.firstElementChild.appendChild(channelNode);
}

function clearChannelWindow(UINode) {
	let windowNode = UINode.mainChatPage.lastElementChild.children[1];
	while (windowNode.firstChild) {
		windowNode.removeChild(windowNode.firstChild);
	}
}

function createChannelIfNotExistAndAppend(UINode, channelName, nick, content) {
	if (UINode.channelWindows[channelName])
		appendToWindow(UINode.channelWindows[channelName], nick, content);
	else {
		addNewChannelWindow(UINode, channelName);
		appendToWindow(UINode.channelWindows[channelName], nick, content);
	}
}

function createPrivateIfNotExistAndAppend(UINode, nick, content) {
	if (UINode.privateMessageWindows[nick]) 
		appendToWindow(UINode.privateMessageWindows[nick], nick, content);
	else {
		addNewPrivateWindow(UINode, nick);
		appendToWindow(UINode.privateMessageWindows[nick], nick, content);
	}
}

function addNamesToChannelUserList(UINode, channelName, names) {
	console.log(names.split(" "));
	if (UINode.channelWindows[channelName]) {
		let nameList = names.split(" ");
		for (let i = 0; i < nameList.length; ++i) {
			let nameElement = document.createElement("div");
			nameElement.addEventListener("click", () => setSelectedUser(UINode.channelWindows[channelName], nameElement));
			nameElement.name = nameList[i];
			nameElement.appendChild(document.createTextNode(nameList[i]));
			UINode.channelWindows[channelName].lastElementChild.firstElementChild.appendChild(nameElement);
		}
	}
}

//Handle IRC messages
function handleMessage(message) {
	switch(message.type) {
	
	case "NOTICE":
		appendToChatWindow(this.UINode, message.sender, message.trailer);
		break;
	case "PRIVMSG":
		// Handle private messages
		if (message.args[0] == this.nick) {
			createPrivateIfNotExistAndAppend(this.UINode, message.sender.substr(0, message.sender.indexOf("!")), message.trailer);
			break;
		}
		createChannelIfNotExistAndAppend(this.UINode, message.args[0], message.sender.substr(0, message.sender.indexOf("!")), message.trailer);
		break;
	case "JOIN":
		if (message.trailer) {
			createChannelIfNotExistAndAppend(this.UINode, message.trailer, "SERVER",
					message.sender.substr(0, message.sender.indexOf("!")) + " has joined the channel");
		}
		else {
			createChannelIfNotExistAndAppend(this.UINode, message.args[0], "SERVER",
					message.sender.substr(0, message.sender.indexOf("!")) + " has joined the channel");
		}
		break;
	case "PART":
		createChannelIfNotExistAndAppend(this.UINode, message.trailer, "SERVER",
				message.sender.substr(0, message.sender.indexOf("!")) + " has left the channel");
		break;
	// Greeting messages
	case "001": case "002": case "003": case "251": case "255": case "265":
	case "266":
		appendToChatWindow(this.UINode, "GREETING", message.trailer);
		break;
	// Number of ops
	case "252":
		appendToChatWindow(this.UINode, "SERVER", message.args[0] + " " + message.trailer);
		break;
	// Channel user list command
	case "353":
		addNamesToChannelUserList(this.UINode, message.args[2], message.trailer);
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
		break;
	case "322":
		appendToChannelWindow(this.UINode, message.args[1]);
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
	let nick_irc_message = "NICK " + socket.nick + "\r\n";
	let nick_json = {type: "SEND-IRC-MESSAGE", message: nick_irc_message};
	let realname = socket.realname;
	if (realname === "") {
		realname = '*';
	}
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
