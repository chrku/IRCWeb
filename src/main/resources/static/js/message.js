
// WS socket
var socket = null;

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

// Attempt to connect to WS server
function attemptConnection() {

	let url = getWSURL();
    // We use a custom protocol
    let socket = new WebSocket(url);

    // Once the connection is established, we send a connect message
    socket.onopen = function(event) {

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

/*
 * This will fire once the DOM is initialized
 */
document.addEventListener("DOMContentLoaded", function() {
	  showSpinner();
	  socket = attemptConnection();
});