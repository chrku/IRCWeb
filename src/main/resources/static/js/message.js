// WS message queue
let msgQueue = []

// Get the url for the WS protocol
function getWSURL() {
    let ws = "";
    if (window.location.protocol === "https:") 
        ws += "wss://";
    else
        ws += "ws://";
    ws += window.location.host + "/ws";
    return ws;
}




// Attempt to connect to WS server
function attemptConnection() {
    let url = getWSURL();

    // We use a custom protocol
    var socket = new WebSocket(getWSURL(), "");

    // Once the connection is established, we send a connect message
    socket.onopen = function WSMessageLoop(event) {
            // Check if there are pending messages
    if (msgQueue.length > 0) {
        let msg = msgQueue.pop();


    }
    }

    return socket;
}

// Wait for websocket rea