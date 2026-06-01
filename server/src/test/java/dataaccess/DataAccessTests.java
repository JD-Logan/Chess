package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class DataAccessTests {

    private MySqlDataAccess dao;

    @BeforeEach
    void setUp() throws DataAccessException {
        dao = new MySqlDataAccess();
        dao.clear();
    }

    @Test
    void smokeTest() throws DataAccessException, SQLException {
        DatabaseManager.createDatabase();

        try (var conn = DatabaseManager.getConnection();
        var ps = conn.prepareStatement("SELECT 1");
        var rs = ps.executeQuery()) {
            Assertions.assertTrue(rs.next());
            Assertions.assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void mySqlDataAccessTableConstructor() throws DataAccessException {
        new MySqlDataAccess();
    }

    @Test
    void clearPositive() throws DataAccessException {
        MySqlDataAccess dao = new MySqlDataAccess();
        dao.createUser(new UserData("JD", "password", "email@me.com"));
        dao.clear();
        Assertions.assertNull(dao.getUser("JD"));
        // bare minimum. will rewrite to check if inserting a user, then clearing, results in getUser being null
    }

    @Test
    void createUserSuccess() throws DataAccessException {
        UserData userInput = new UserData("JD", "password", "email@me.com");
        dao.createUser(userInput);

        UserData dbVersion = dao.getUser("JD");
        Assertions.assertNotNull(dbVersion);
        Assertions.assertEquals("JD", dbVersion.username());
        Assertions.assertNotEquals("password", dbVersion.password());
        Assertions.assertTrue(BCrypt.checkpw("password", dbVersion.password()));
    }

    @Test
    void createUserFailure() throws DataAccessException {
        // duplicate username
        dao.createUser(new UserData("JD", "password 1", "email2@me.com"));

        Assertions.assertThrows(DataAccessException.class, () ->
                dao.createUser(new UserData("JD", "password 2", "email2@me.com")));
    }

    @Test
    void authCreateAndGetSuccess() throws DataAccessException {
        dao.createUser(new UserData("JD", "password", "email@me.com"));
        AuthData auth = dao.createAuth("JD");

        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.authToken());

        AuthData dbVersion = dao.getAuth(auth.authToken());
        Assertions.assertNotNull(dbVersion);

    }

    @Test
    void deleteAuthSuccess() throws DataAccessException {
        dao.createUser(new UserData("JD", "password", "email@me.com"));
        AuthData auth = dao.createAuth("JD");

        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.authToken());

        dao.deleteAuth(auth.authToken());
        Assertions.assertNull(dao.getAuth(auth.authToken()));
    }

    @Test
    void badTokenReturnsNull() throws DataAccessException {
        Assertions.assertNull(dao.getAuth("NotAnActualToken"));
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        GameData game = dao.createGame("Test Game Name");

        Assertions.assertTrue(game.gameID() > 0);
        Assertions.assertNull(game.whiteUsername());
        Assertions.assertNull(game.blackUsername());
        Assertions.assertEquals("Test Game Name", game.gameName());
        Assertions.assertNull(game.game());
    }

    @Test
    void createGameFailure() {
    }

    @Test
    void getGameSuccess() throws DataAccessException {
        GameData fromClient = dao.createGame("Test Game");
        GameData fromDB = dao.getGame(fromClient.gameID());

        Assertions.assertNotNull(fromDB);
        Assertions.assertEquals(fromClient.gameID(), fromDB.gameID());
        Assertions.assertEquals("Test Game", fromDB.gameName());
        Assertions.assertNull(fromDB.whiteUsername());
        Assertions.assertNull(fromDB.blackUsername());
    }

    @Test
    void getGameFailure() throws DataAccessException {
        Assertions.assertNull(dao.getGame(9999999));
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        Collection<GameData> gamesEmpty = dao.listGames();

        Assertions.assertTrue(gamesEmpty.isEmpty());

        dao.createGame("one game");
        dao.createGame("two games");
        dao.createGame("three games");

        Collection<GameData> gamesNotEmpty = dao.listGames();

        Assertions.assertEquals(3, gamesNotEmpty.size());
    }

    @Test
    void updateGameSuccess_whiteUsername() throws DataAccessException {
        GameData freshGame = dao.createGame("Test Game");
        GameData updatedGame = new GameData(
                freshGame.gameID(),
                "JD",
                null,
                freshGame.gameName(),
                null
        );
        dao.updateGame(updatedGame);

        GameData fromDB = dao.getGame(freshGame.gameID());
        Assertions.assertEquals("JD", fromDB.whiteUsername());
        Assertions.assertNull(fromDB.blackUsername());
    }

    @Test
    void updateGameSuccess_BlackUsername() throws DataAccessException {
        GameData freshGame = dao.createGame("Test Game");
        GameData updatedGame = new GameData(
                freshGame.gameID(),
                null,
                "Tay",
                freshGame.gameName(),
                null
        );
        dao.updateGame(updatedGame);

        GameData fromDB = dao.getGame(freshGame.gameID());
        Assertions.assertEquals("Tay", fromDB.blackUsername());
        Assertions.assertNull(fromDB.whiteUsername());
    }

    @Test
    void chessGamePersistance() throws DataAccessException {
        GameData freshGame = dao.createGame("test");
//        ChessGame
    }

}
