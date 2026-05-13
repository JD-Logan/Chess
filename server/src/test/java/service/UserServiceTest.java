package service;

import dataaccess.MemoryDataAccess;
import org.junit.jupiter.api.*;

public class UserServiceTest {

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
    void registerSuccess() {}

    @Test
    void registerFailure() {}

    @Test
    void loginSuccess() {}

    @Test
    void loginFailure() {}

    @Test
    void logoutSuccess() {}

    @Test
    void logoutFailure() {}

}
