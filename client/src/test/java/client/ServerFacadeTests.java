package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {facade.clear();}

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void registerPositive() throws Exception {
        var auth =
                facade.register(
                        "player1",
                        "password:",
                        "player1@email.com");
        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.authToken());
    }

    @Test
    public void registerNegative() throws Exception {
        facade.register(
                "player1",
                "password",
                "email");
        Assertions.assertThrows(
                Exception.class,
                () -> facade.register(
                        "player1",
                        "password",
                        "email"));
    }

    @Test
    public void loginPosition() throws Exception {
        facade.register(
                "player1",
                "password",
                "email");
        var auth =
                facade.login(
                        "player1",
                        "password");
        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.authToken());
    }

    @Test
    public void loginNegative() {
        Assertions.assertThrows(
                Exception.class,
                () -> facade.login(
                        "player1",
                        "wrongPassword"));
    }

    @Test
    public void logoutPositive() throws Exception {
        var auth =
                facade.register(
                        "player1",
                        "password",
                        "email");
        facade.logout(auth.authToken());
        Assertions.assertTrue(true);
    }

    @Test
    public void logoutNegative() {
        Assertions.assertThrows(
                Exception.class,
                () -> facade.logout("fake-token"));
    }

    @Test
    public void listGamesPositive() throws Exception {
        var auth =
                facade.register(
                        "player1",
                        "password",
                        "email");
        facade.createGame(
                auth.authToken(),
                "Game One");
        var result =
                facade.listGames(
                        auth.authToken());
        Assertions.assertEquals(
                1,
                result.games().size());
    }

    @Test
    public void listGamesNegative() {

        Assertions.assertThrows(
                Exception.class,
                () -> facade.listGames(
                        "invalid-token"));
    }

    @Test
    public void createGamePositive() throws Exception {
        var auth =
                facade.register(
                        "player1",
                        "password",
                        "email");
        var result =
                facade.createGame(
                        auth.authToken(),
                        "Test Game");
        Assertions.assertTrue(
                result.gameID() > 0);
    }

    @Test
    public void createGameNegative() {
        Assertions.assertThrows(
                Exception.class,
                () -> facade.createGame(
                        "bad-token",
                        "Test Game"));
    }

}
