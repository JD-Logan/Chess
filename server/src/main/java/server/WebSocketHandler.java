package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;


public class WebSocketHandler {
    private final DataAccess dataAccess;
    private final Gson gson;
    private final ConnectionManager connections = new ConnectionManager();

    public WebSocketHandler(DataAccess dataAccess, Gson gson) {
        this.dataAccess = dataAccess;
        this.gson = gson;
    }

    public void onConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
    }

    public void onMessage(WsMessageContext ctx) {
        UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> connect(command, ctx);
            case MAKE_MOVE -> {}
            case LEAVE -> {}
            case RESIGN -> {}
        }
    }

    public void onClose(WsCloseContext ctx) {

    }

    private void sendError(WsMessageContext ctx, String message) {
        ctx.send(gson.toJson(new ErrorMessage(message)));
    }

    private void connect(UserGameCommand command, WsMessageContext ctx) {
        try {
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: unauthorized");
                return;
            }
            GameData game = dataAccess.getGame(command.getGameID());
            if (game == null) {
                sendError(ctx, "Error: bad request");
                return;
            }
            String username = auth.username();
            int gameID = command.getGameID();
            connections.add(ctx.sessionId(), username, gameID, ctx);
            connections.send(ctx.sessionId(), gson.toJson(new LoadGameMessage(game)));
//            String notification = buildConnectNotification(username, game);
            String notification = username + " connected as observer";
            if (username.equals(game.whiteUsername())) {
                notification = username + " connected as WHITE";
            }
            if (username.equals(game.blackUsername())) {
                notification = username + " connected as BLACK";
            }

            connections.broadcastToGameExcept(
                    gameID,
                    ctx.sessionId(),
                    gson.toJson(new NotificationMessage(notification))
            );
        } catch (DataAccessException e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private String buildConnectNotification(String username, GameData game) {}

}
