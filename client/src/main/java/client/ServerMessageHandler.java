package client;

import websocket.messages.ServerMessage;

public interface ServerMessageHandler {
    void onMessage(ServerMessage message);
}
