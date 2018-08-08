
// WS socket
let socket = null;
let realname = null;
let nick = null;

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
	document.getElementById("login").style.visibility = "visible";
}

function hideLoginPage() {
	document.getElementById("login").style.visibility = "hidden";
}

function showSpinner() {
	document.getElementById("spin").style.visibility = "visible";
}

function hideSpinner() {
	document.getElementById("spin").style.visibility = "hidden";
}

function showFailure() {
	document.getElementById("failure").style.visibility = "visible";
}

function hideFailure() {
	document.getElementById("failure").style.visibility = "hidden";
}

function showLoginError() {
	document.getElementById("login-error").style.visibility = "visible";
}

function hideLoginError() {
	document.getElementById("login-error").style.visibility = "hidden";
}

// Attempt to connect to WS server
function attemptConnection() {

	let url = getWSURL();
    // We use a custom protocol
    let socket = new WebSocket(url);

    // Once the connection is established, we send a connect message
    socket.onopen = function(event) {
    	// Ping the server every 3 seconds
    	 setInterval(
    			 function() { socket.send(JSON.stringify(PING_QUERY)); }, 3000);
    }
    
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
	let server_query = {type: "CONNECTION-ATTEMPT", args: [hostname, port]};
	let ws_query = JSON.stringify(server_query);
	socket.send(ws_query);
}

/*
 * This will fire once the DOM is initialized
 */
document.addEventListener("DOMContentLoaded", function() {
	  showSpinner();
	  socket = attemptConnection();
});