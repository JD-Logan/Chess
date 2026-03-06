package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new SecurityException("unauthorized");
        }
        return dataAccess.listGames();
    }

    public GameData createGame(String authToken, String gameName) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new SecurityException("unauthorized");
        }
        if (gameName == null) {
            throw new IllegalArgumentException("missing fields");
        }
        return dataAccess.createGame(gameName);
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new SecurityException("unauthorized");
        }
        GameData game = dataAccess.getGame(gameID);
        if (game == null || playerColor == null || (!playerColor.equals("WHITE") && !playerColor.equals("BLACK"))) {
            throw new IllegalArgumentException("bad request");
        }
        if (playerColor.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new IllegalStateException("already taken");
            }
            dataAccess.updateGame(new GameData(game.gameID(), auth.username(),
                    game.blackUsername(), game.gameName(), game.game()));
        } else {
            if (game.blackUsername() != null) {
                throw new IllegalStateException("already taken");
            }
            dataAccess.updateGame(new GameData(game.gameID(), game.whiteUsername(),
                    auth.username(), game.gameName(), game.game()));
        }

    }
}
