package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import chess.ChessGame;
import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;

    private ClearService clearService;

    @BeforeEach
    public void setup() {

        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    @DisplayName("Clear Positive")
    public void clearPositive() throws Exception {

        // add user
        userDAO.createUser(
                new UserData("player1", "password", "email"));

        // add auth
        authDAO.createAuth(
                new AuthData("token123", "player1"));

        // add game
        ChessGame chessGame = new ChessGame();

        int gameID = gameDAO.createGame(
                new GameData(
                        0,
                        null,
                        null,
                        "Test Game",
                        chessGame
                )
        );

        // verify data exists before clear
        assertNotNull(userDAO.getUser("player1"));
        assertNotNull(authDAO.getAuth("token123"));
        assertNotNull(gameDAO.getGame(gameID));

        // clear all data
        clearService.clear();

        // verify all data removed
        assertNull(userDAO.getUser("player1"));
        assertNull(authDAO.getAuth("token123"));
        assertNull(gameDAO.getGame(gameID));

        // verify game IDs reset properly
        int newGameID = gameDAO.createGame(
                new GameData(
                        0,
                        null,
                        null,
                        "New Game",
                        new ChessGame()
                )
        );

        assertEquals(1, newGameID);
    }
}
