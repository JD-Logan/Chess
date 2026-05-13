package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

public class ClearServiceTest {

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
    void clear_removes_data_completely() throws DataAccessException {
        // put data in database
        // register user and get authToken
        AuthData auth = userService.register(new UserData("JD", "password", "email@me.com"));
        String token = auth.authToken();

        // assert that the data is in the database
        // create game
        // assert that game is in the database

        // do clear()
        clearService.clear();

        // assert that all the data is gone from database

        // try putting new data in db and testing if it is there

    }
}
