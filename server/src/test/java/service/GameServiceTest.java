package service;

import dataaccess.MemoryDataAccess;
import org.junit.jupiter.api.*;

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
    void listGamesSuccess() {}

    @Test
    void listGamesFailure() {}

    @Test
    void createGameSuccess() {}

    @Test
    void createGameFailure() {}

    @Test
    void joinGameSuccess() {}

    @Test
    void joinGameFailure() {}
}
