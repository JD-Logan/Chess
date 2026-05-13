package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.Collection;

public class GameServiceTest {

    private MemoryDataAccess dataAccess;
    private ClearService clearService;
    private UserService userService;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        AuthData auth = userService.register(new UserData("JD", "password", "email@me.com"));
        String token = auth.authToken();
        Collection<GameData> games = gameService.listGames(token);

        // assert games start empty
        Assertions.assertTrue(games.isEmpty());

        // assert adding a game makes not empty
        gameService.createGame(token, "Test Game");
        Collection<GameData> afterCreateGames = gameService.listGames(token);

        Assertions.assertEquals(1,afterCreateGames.size());

        // assert added game name is in list
        GameData game = afterCreateGames.iterator().next();
        Assertions.assertEquals("Test Game", game.gameName());


    }

    @Test
    void listGamesFailure() {
        Assertions.assertThrows(SecurityException.class, () ->
                gameService.listGames("Bad Token"));
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        AuthData auth = userService.register(new UserData("JD", "password", "email@me.com"));
        String token = auth.authToken();
        GameData game = gameService.createGame(token, "Test Game");

        Assertions.assertEquals(1, game.gameID());
        Assertions.assertEquals("Test Game", game.gameName());
        Assertions.assertNotNull(dataAccess.getGame(1));
    }

    @Test
    void createGameFailure() throws DataAccessException {
        AuthData auth = userService.register(new UserData("JD", "password", "email@me.com"));
        String token = auth.authToken();
        GameData game = gameService.createGame(token, "Test Game");

        Assertions.assertEquals(1, game.gameID());
        Assertions.assertEquals("Test Game", game.gameName());
        Assertions.assertNotNull(dataAccess.getGame(1));

        // invalid authToken
        Assertions.assertThrows(SecurityException.class, () ->
                gameService.createGame("Bad Token", "Test Game 2")
        );

        // invalid gameName
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                gameService.createGame(token, null)
        );
    }

    @Test
    void joinGameSuccess() throws DataAccessException {
        AuthData auth1 = userService.register(new UserData("JD", "password", "email@me.com"));
        String token1 = auth1.authToken();
        GameData game = gameService.createGame(token1, "Test Game");
        AuthData auth2 = userService.register(new UserData("Taylor", "password", "email@me.com"));
        String token2 = auth2.authToken();

        // White join
        gameService.joinGame(token1, "WHITE", game.gameID());
        GameData updatedGame1 = dataAccess.getGame(game.gameID());
        Assertions.assertEquals("JD", updatedGame1.whiteUsername());

        // Black join
        gameService.joinGame(token2, "BLACK", game.gameID());
        GameData updatedGame2 = dataAccess.getGame(game.gameID());
        Assertions.assertEquals("Taylor", updatedGame2.blackUsername());

    }

    @Test
    void joinGameFailure() throws DataAccessException {
        AuthData auth1 = userService.register(new UserData("JD", "password", "email@me.com"));
        String token1 = auth1.authToken();
        GameData game = gameService.createGame(token1, "Test Game");
        AuthData auth2 = userService.register(new UserData("Taylor", "password", "email@me.com"));
        String token2 = auth2.authToken();

        // join with bad authToken
        Assertions.assertThrows(SecurityException.class, () ->
                gameService.joinGame("Bad Token", "WHITE", game.gameID())
        );

        // White join twice
        gameService.joinGame(token1, "WHITE", game.gameID());
        Assertions.assertThrows(IllegalStateException.class, () ->
                gameService.joinGame(token2, "WHITE", game.gameID())
        );

        // invalid color join
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                gameService.joinGame(token1, "GREY", game.gameID())
        );

        // invalid gameID join
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                gameService.joinGame(token1, "WHITE", 999)
        );
    }
}
