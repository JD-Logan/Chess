package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.MakeMoveCommand;
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
            case MAKE_MOVE -> makeMove(gson.fromJson(ctx.message(), MakeMoveCommand.class), ctx);
            case LEAVE -> {}
            case RESIGN -> {}
        }
    }

    public void onClose(WsCloseContext ctx) {
        connections.remove(ctx.sessionId());
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
            String notification = buildConnectNotification(username, game);

            connections.broadcastToGameExcept(
                    gameID,
                    ctx.sessionId(),
                    gson.toJson(new NotificationMessage(notification))
            );
        } catch (DataAccessException e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private String buildConnectNotification(String username, GameData game) {
        String notification = username + " connected as observer";
        if (username.equals(game.whiteUsername())) {
            notification = username + " connected as WHITE";
        }
        if (username.equals(game.blackUsername())) {
            notification = username + " connected as BLACK";
        }
        return notification;
    }

    private final Object moveLock = new Object();

    private void makeMove(MakeMoveCommand command, WsMessageContext ctx) {
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

            if (!isPlayer(username, game)) {
                sendError(ctx, "Error: observers cannot make moves");
                return;
            }

            GameData updatedGame;
            synchronized (moveLock) {
                game = dataAccess.getGame(gameID);
                ChessGame chessGame = game.game();
                if (isGameOver(chessGame)) {
                    sendError(ctx, "Error: game over");
                    return;
                }

                chessGame.makeMove(command.getMove());
                updatedGame = new GameData(
                        game.gameID(),
                        game.whiteUsername(),
                        game.blackUsername(),
                        game.gameName(),
                        chessGame
                );
                dataAccess.updateGame(updatedGame);
            }

            broadcastAfterMove(gameID, ctx.sessionId(), username, updatedGame);

        } catch (InvalidMoveException | DataAccessException e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private boolean isPlayer(String username, GameData game) {
        return username.equals(game.whiteUsername()) || username.equals(game.blackUsername());
    }

    private void broadcastAfterMove(int gameID, String moverSessionID, String username, GameData updated) {
        String loadJson = gson.toJson(new LoadGameMessage(updated));
        connections.broadcastToGame(gameID, loadJson);

        connections.broadcastToGameExcept(
                gameID,
                moverSessionID,
                gson.toJson(new NotificationMessage(username + " made a move"))
        );

        if (isCheckOrGameOver(updated.game())) {
            connections.broadcastToGame(gameID, gson.toJson(new NotificationMessage(buildGameStatusMessage(updated.game()))));
        }

    }

    private boolean isGameOver(ChessGame chessGame) {
        return chessGame.isInCheckmate(ChessGame.TeamColor.WHITE) ||
                chessGame.isInCheckmate(ChessGame.TeamColor.BLACK) ||
                chessGame.isInStalemate(ChessGame.TeamColor.WHITE) ||
                chessGame.isInStalemate(ChessGame.TeamColor.BLACK);
    }

    private boolean isCheckOrGameOver(ChessGame chessGame) {
        return chessGame.isInCheck(ChessGame.TeamColor.WHITE)
                || chessGame.isInStalemate(ChessGame.TeamColor.WHITE)
                || chessGame.isInCheckmate(ChessGame.TeamColor.WHITE)
                || chessGame.isInCheck(ChessGame.TeamColor.BLACK)
                || chessGame.isInStalemate(ChessGame.TeamColor.BLACK)
                || chessGame.isInCheckmate(ChessGame.TeamColor.BLACK);
    }

    private String buildGameStatusMessage(ChessGame chessGame) {
        if (chessGame.isInCheck(ChessGame.TeamColor.WHITE)) {
            return "WHITE is in check";
        }
        if (chessGame.isInStalemate(ChessGame.TeamColor.WHITE)) {
            return "WHITE is in Stalemate";
        }
        if (chessGame.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            return "WHITE is in Checkmate";
        }
        if (chessGame.isInCheck(ChessGame.TeamColor.BLACK)) {
            return "BLACK is in Check";
        }
        if (chessGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
            return "BLACK is in Stalemate";
        }
        if (chessGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            return "BLACK is in Checkmate";
        }
        return "game status has updated";
    }

}
