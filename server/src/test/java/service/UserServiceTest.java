package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.Objects;

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
    void registerSuccess() throws DataAccessException {
        AuthData auth = userService.register(new UserData("JD", "password", "email@me.com"));
        String token = auth.authToken();

        Assertions.assertNotNull(dataAccess.getUser("JD"));
        Assertions.assertNotNull(dataAccess.getAuth(token));

    }

    @Test
    void registerFailure_duplicateUsername() throws DataAccessException {

//        AuthData auth = userService.register(new UserData("JD", "password", "email@me.com"));
//        String token1 = auth.authToken();
//
//        AuthData auth2 = userService.register(new UserData("JD", "password2", "email2@me.com"));
//        String token2 = auth2.authToken();
//
//        Assertions.assertNull(dataAccess.getUser("JD"));
//        Assertions.assertNull(dataAccess.getAuth(token2));

//        Assertions.assertThrows(IllegalStateException.class());

        // duplicate usernames

        // register a new user1
        userService.register(new UserData("JD", "password", "email@me.com"));
        // Assert assertThrows the exception
        Assertions.assertThrows(IllegalStateException.class, () ->
                userService.register(new UserData("JD", "password2", "email2@me.com"))
        );
                // register a user2

        // missing fields
        //
    }

    @Test
    void registerFailure_missingFields() throws DataAccessException {
        // missing username
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                userService.register(new UserData(null, "password", "email@me.com"))
        );
        // missing password
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                userService.register(new UserData("JD", null, "email@me.com"))
        );
        // missing email
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                userService.register(new UserData("JD", "password", null))
        );
    }


    @Test
    void loginSuccess() throws DataAccessException {
        userService.register(new UserData("JD", "password", "email@me.com"));
        AuthData auth = userService.login("JD", "password");

        Assertions.assertNotNull(auth.authToken());
        Assertions.assertEquals("JD", auth.username());
        Assertions.assertNotNull(dataAccess.getAuth(auth.authToken()));
    }

    @Test
    void loginFailure() throws DataAccessException {
        // can split into three tests if I want more specific error help
        // bad password
        userService.register(new UserData("JD", "password", "email@me.com"));
        // assert
        Assertions.assertThrows(SecurityException.class, () ->
                // login with bad password
                userService.login("JD", "password1")
        );

        // unknown user
        Assertions.assertThrows(SecurityException.class, () ->
                userService.login("Not JD", "password")
        );

        // null fields (username)
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                userService.login(null, "password")
        );

        // null fields (pw)
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                userService.login("Not JD", null)
        );
    }

    @Test
    void logoutSuccess() throws DataAccessException {
        userService.register(new UserData("JD", "password", "email@me.com"));
        AuthData auth = userService.login("JD", "password");
        userService.logout(auth.authToken());

        Assertions.assertNull(dataAccess.getAuth(auth.authToken()));
    }

    @Test
    void logoutFailure() throws DataAccessException {
//        userService.logout("Fake Token");

        Assertions.assertThrows(SecurityException.class, () ->
                userService.logout("Fake Token")
        );
    }

}
