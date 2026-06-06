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
        var port = server.run(0);
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
    }

    @Test
    public void clearFailure() throws Exception {

    }
}
