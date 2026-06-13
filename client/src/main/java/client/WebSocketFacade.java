package client;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.websocket.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.net.URI;


public class WebSocketFacade extends Endpoint {
    private Session session;
    private final Gson gson = new Gson();
    private final ServerMessageHandler handler;

    public WebSocketFacade(String serverUrl, ServerMessageHandler handler) throws Exception {
        this.handler = handler;
        URI uri = new URI(serverUrl.replace("http", "ws") + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        session.addMessageHandler(String.class, this::handleMessage);
    }

    private void handleMessage(String message) {
        JsonObject json = JsonParser.parseString(message).getAsJsonObject();
        String type = json.get("serverMessageType").getAsString();
        ServerMessage msg = switch (ServerMessage.ServerMessageType.valueOf(type)) {
            case LOAD_GAME -> gson.fromJson(message, LoadGameMessage.class);
            case ERROR -> gson.fromJson(message, ErrorMessage.class);
            case NOTIFICATION -> gson.fromJson(message, NotificationMessage.class);
        };
        handler.onMessage(msg);
    }

    public void sendConnect(String authToken, int gameID) throws Exception {
        send(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }

    public void sendMove(String authToken, int gameID, chess.ChessMove move) throws Exception {
        send(new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move));
    }

    public void sendLeave(String authToken, int gameID) throws Exception {
        send(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
    }

    public void sendResign(String authToken, int gameID) throws Exception {
        send(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
    }

    private void send(Object command) throws Exception {
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void disconnect() throws Exception {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
