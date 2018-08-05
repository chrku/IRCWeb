
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




// Attempt to connect to WS server
function attemptConnection() {

	let url = getWSURL();
    // We use a custom protocol
    let socket = new WebSocket(url);

    // Once the connection is established, we send a connect message
    socket.onopen = function(event) {
    		document.getElementById("login").style.visibility = "visible";
    }
    
    socket.onmessage = function(event) {
    	console.log(event.data);
    }

    return socket;
}

/*
 * This will fire once the DOM is initialized
 */
document.addEventListener("DOMContentLoaded", function() {
	  socket = attemptConnection();
});