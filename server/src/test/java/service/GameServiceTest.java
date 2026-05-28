package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import service.requests.*;
import service.results.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private GameService gameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    private final String VALID_TOKEN = "validToken";

    @BeforeEach
    public void setup() throws Exception {

        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();

        gameService = new GameService(gameDAO, authDAO);

        authDAO.createAuth(new AuthData(VALID_TOKEN, "player1"));
    }

    // ---------------- CREATE GAME TESTS ----------------

    @Test
    @DisplayName("Create Game Positive")
    public void createGamePositive() throws Exception {

        CreateGameRequest request =
                new CreateGameRequest("Test Game", VALID_TOKEN);

        CreateGameResult result =
                gameService.createGame(request);

        assertNotNull(result);
        assertTrue(result.gameID() > 0);

        GameData game =
                gameDAO.getGame(result.gameID());

        assertNotNull(game);
        assertEquals("Test Game", game.gameName());
    }

    @Test
    @DisplayName("Create Game Negative Unauthorized")
    public void createGameNegativeUnauthorized() {

        CreateGameRequest request =
                new CreateGameRequest("Test Game", "badToken");

        Exception ex = assertThrows(Exception.class, () ->
                gameService.createGame(request));

        assertEquals("unauthorized", ex.getMessage());
    }

    // ---------------- LIST GAMES TESTS ----------------

    @Test
    @DisplayName("List Games Positive")
    public void listGamesPositive() throws Exception {

        gameService.createGame(
                new CreateGameRequest("Game1", VALID_TOKEN));

        gameService.createGame(
                new CreateGameRequest("Game2", VALID_TOKEN));

        ListGamesResult result =
                gameService.listGames(
                        new ListGamesRequest(VALID_TOKEN));

        Collection<GameData> games = result.games();

        assertNotNull(games);
        assertEquals(2, games.size());
    }

    @Test
    @DisplayName("List Games Negative Unauthorized")
    public void listGamesNegativeUnauthorized() {

        Exception ex = assertThrows(Exception.class, () ->
                gameService.listGames(
                        new ListGamesRequest("badToken")));

        assertEquals("unauthorized", ex.getMessage());
    }

    // ---------------- JOIN GAME TESTS ----------------

    @Test
    @DisplayName("Join Game Positive")
    public void joinGamePositive() throws Exception {

        CreateGameResult createResult =
                gameService.createGame(
                        new CreateGameRequest("TestGame", VALID_TOKEN));

        JoinGameRequest request =
                new JoinGameRequest("WHITE",
                        createResult.gameID(),
                        VALID_TOKEN);

        gameService.joinGame(request);

        GameData updatedGame =
                gameDAO.getGame(createResult.gameID());

        assertEquals("player1", updatedGame.whiteUsername());
        assertNull(updatedGame.blackUsername());
    }

    @Test
    @DisplayName("Join Game Negative Already Taken")
    public void joinGameNegativeTaken() throws Exception {

        CreateGameResult createResult =
                gameService.createGame(
                        new CreateGameRequest("TestGame", VALID_TOKEN));

        JoinGameRequest request =
                new JoinGameRequest("WHITE",
                        createResult.gameID(),
                        VALID_TOKEN);

        gameService.joinGame(request);

        Exception ex = assertThrows(Exception.class, () ->
                gameService.joinGame(request));

        assertEquals("already taken", ex.getMessage());
    }
    @Test
    @DisplayName("Join Game Invalid Color")
    public void joinGameInvalidColor() throws Exception {

        CreateGameResult createResult =
                gameService.createGame(
                        new CreateGameRequest("TestGame", VALID_TOKEN));

        JoinGameRequest request =
                new JoinGameRequest("GREEN",
                        createResult.gameID(),
                        VALID_TOKEN);

        Exception ex = assertThrows(Exception.class, () ->
                gameService.joinGame(request));

        assertEquals("bad request", ex.getMessage());
    }
    @Test
    @DisplayName("Join Game Null GameID")
    public void joinGameNullGameID() {

        JoinGameRequest request =
                new JoinGameRequest("WHITE",
                        null,
                        VALID_TOKEN);

        Exception ex = assertThrows(Exception.class, () ->
                gameService.joinGame(request));

        assertEquals("bad request", ex.getMessage());
    }
}
