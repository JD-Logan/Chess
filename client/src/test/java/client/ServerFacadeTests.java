package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;

    private static int port;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        facade = new ServerFacade("localhost", port);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setUp() throws Exception {
        facade.clear();
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void register() throws Exception {
        ServerFacade.AuthResult reg = facade.register("JD", "password", "test@email.com");
        Assertions.assertNotNull(reg.authToken());
        Assertions.assertEquals("JD", reg.username());
    }

    @Test
    public void login() throws Exception {
        ServerFacade.AuthResult reg = facade.register("JD", "password", "test@email.com");
        ServerFacade.AuthResult login = facade.login("JD", "password");
        Assertions.assertNotNull(login.authToken());
        Assertions.assertEquals("JD", login.username());
    }

    @Test
    public void clearSuccess() throws Exception {
        // check status 200
        Assertions.assertDoesNotThrow(() -> facade.clear());
        facade.register("JD", "password", "test@email.com");
    }

    @Test
    public void clearDeletesData() throws Exception {
        ServerFacade.AuthResult auth = facade.register("JD", "password", "test@email.com");
        facade.createGame(auth.authToken(), "game");
        facade.clear();
        Assertions.assertThrows(RuntimeException.class, () ->
                facade.login("JD", "password")
        );
    }

    @Test
    void registerAndCreateAndListAndJoin() throws Exception {
        ServerFacade.AuthResult auth = facade.register("JD", "password", "test@email.com");

        ServerFacade.CreateGameResult testGame = facade.createGame(auth.authToken(), "test game");
        Assertions.assertTrue(testGame.gameID()>0);

        ServerFacade.ListGamesResult gameList = facade.listGames(auth.authToken());
        Assertions.assertEquals(1, gameList.games().size());
        Assertions.assertEquals("test game", gameList.games().getFirst().gameName());
        Assertions.assertNull(gameList.games().getFirst().whiteUsername());

        facade.joinGame(auth.authToken(), "WHITE", testGame.gameID());

        ServerFacade.ListGamesResult gameListAfterJoin = facade.listGames(auth.authToken());
        Assertions.assertEquals("JD", gameListAfterJoin.games().getFirst().whiteUsername());
    }

    @Test
    void registerUnauthorizedBecauseDuplicate() throws Exception {
        facade.register("JD", "password", "test@email.com");

        Assertions.assertThrows(RuntimeException.class, () ->
                facade.register("JD", "differentPassword", "different@email.com")
        );
    }

    @Test
    void createGameUnauthorizedFailure() {
        Assertions.assertThrows(RuntimeException.class, () ->
                facade.createGame("bad-token", "My Game")
        );
    }

    @Test
    void listGamesUnauthorizedFailure() {
        Assertions.assertThrows(RuntimeException.class, () ->
                facade.listGames("bad-token")
        );
    }

    @Test
    void wrongLoginPassword() throws Exception {
        facade.register("JD", "password", "test@email.com");

        Assertions.assertThrows(RuntimeException.class, () ->
                facade.login("JD", "wrongPassword")
        );
    }

    @Test
    void loginSuccess() throws Exception {
        facade.register("JD", "password", "test@email.com");
        ServerFacade.AuthResult auth = facade.login("JD", "password");
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertEquals("JD", auth.username());
    }

    @Test
    void unauthorizedListGames() throws Exception {
        Assertions.assertThrows(RuntimeException.class, () ->
                facade.listGames("badToken")
        );
    }

    @Test
    void joinGameAlreadyTakenFailure() throws Exception {
        ServerFacade.AuthResult auth = facade.register("JD", "password", "test@email.com");
        ServerFacade.CreateGameResult game = facade.createGame(auth.authToken(), "full");
        facade.joinGame(auth.authToken(), "WHITE", game.gameID());
        Assertions.assertThrows(RuntimeException.class, () ->
                facade.joinGame(auth.authToken(), "WHITE", game.gameID())
        );
    }

    @Test
    void logoutSuccess() throws Exception {
        ServerFacade.AuthResult auth = facade.register("JD", "password", "test@email.com");
        Assertions.assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }
    @Test
    void logoutBadTokenFailure() {
        Assertions.assertThrows(RuntimeException.class, () ->
                facade.logout("not-a-real-token")
        );
    }

    @Test
    void createGameSuccess() throws Exception {
        ServerFacade.AuthResult auth = facade.register("JD", "password", "test@email.com");
        ServerFacade.CreateGameResult result = facade.createGame(auth.authToken(), "My Game");
        Assertions.assertTrue(result.gameID() > 0);
    }

    @Test
    void listGamesSuccess() throws Exception {
        ServerFacade.AuthResult auth = facade.register("JD", "password", "test@email.com");
        facade.createGame(auth.authToken(), "one");
        facade.createGame(auth.authToken(), "two");
        ServerFacade.ListGamesResult result = facade.listGames(auth.authToken());
        Assertions.assertEquals(2, result.games().size());
    }

    @Test
    void joinGameSuccess() throws Exception {
        ServerFacade.AuthResult auth = facade.register("JD", "password", "test@email.com");
        ServerFacade.CreateGameResult game = facade.createGame(auth.authToken(), "join me");
        facade.joinGame(auth.authToken(), "WHITE", game.gameID());
        ServerFacade.ListGamesResult list = facade.listGames(auth.authToken());
        Assertions.assertEquals("JD", list.games().getFirst().whiteUsername());
    }


}
