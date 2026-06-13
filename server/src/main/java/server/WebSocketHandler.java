package server;

import chess.ChessGame;
import chess.ChessPiece;
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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class WebSocketHandler {
    private final DataAccess dataAccess;
    private final Gson gson;
    private final ConnectionManager connections = new ConnectionManager();
    private final Object moveLock = new Object();

    private final ConcurrentHashMap<Integer, Object> gameLocks = new ConcurrentHashMap<>();

    private Object getGameLock(int gameID) {
        return gameLocks.computeIfAbsent(gameID, ID -> new Object());
    }


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
            case LEAVE -> leave(command, ctx);
            case RESIGN -> resign(command, ctx);
        }
    }

    private final Set<Integer> resignedGames = ConcurrentHashMap.newKeySet();

    private void resign(UserGameCommand command, WsMessageContext ctx) {
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
                sendError(ctx, "Error: observers cannot resign from the game");
                return;
            }

            if (resignedGames.contains(gameID)) {
                sendError(ctx, "Error: game is already over");
                return;
            }


            synchronized (getGameLock(gameID)) {
                if (resignedGames.contains(gameID)) {
                    sendError(ctx, "Error: game is already over");
                    return;
                }
                resignedGames.add(gameID);
                dataAccess.updateGame(game);
            }

            connections.broadcastToGame(
                    game.gameID(),
                    gson.toJson(new NotificationMessage(username + " resigned from the game"))
            );

        } catch (DataAccessException e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void leave(UserGameCommand command, WsMessageContext ctx) {
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

            synchronized (getGameLock(gameID)) {
                game = dataAccess.getGame(gameID);
                String white = game.whiteUsername();
                String black = game.blackUsername();
                if (username.equals(white) || username.equals(black)) {
                    if (username.equals(white)) {
                        white = null;
                    }
                    if (username.equals(black)) {
                        black = null;
                    }
                    dataAccess.updateGame(new GameData(
                            game.gameID(),
                            white,
                            black,
                            game.gameName(),
                            game.game()
                    ));
                }
            }

            connections.remove(ctx.sessionId());
            connections.broadcastToGame(
                    game.gameID(),
                    gson.toJson(new NotificationMessage(username + " left the game"))
            );

        } catch (DataAccessException e) {
            sendError(ctx, "Error: " + e.getMessage());
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
            synchronized (getGameLock(gameID)) {
                game = dataAccess.getGame(gameID);
                ChessGame chessGame = game.game();

                if (resignedGames.contains(gameID)) {
                    sendError(ctx, "Error: game is already over");
                    return;
                }

                if (isGameOver(chessGame)) {
                    sendError(ctx, "Error: game over");
                    return;
                }

                ChessGame.TeamColor playerColor = username.equals(game.whiteUsername())
                        ? ChessGame.TeamColor.WHITE
                        : ChessGame.TeamColor.BLACK;

                ChessPiece piece = chessGame.getBoard().getPiece((command.getMove().getStartPosition()));
                if (piece != null && piece.getTeamColor() != playerColor) {
                    sendError(ctx, "Error: Cannot move your opponents pieces");
                    return;
                }

                if (chessGame.getTeamTurn() != playerColor) {
                    sendError(ctx, "Error: Not your turn");
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
