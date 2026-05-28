package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTests {

    private SQLGameDAO gameDAO;

    @BeforeEach
    public void setUp() throws Exception {
        gameDAO = new SQLGameDAO();
        gameDAO.clear();
    }

    @Test
    @DisplayName("Create Game Positive")
    public void createGamePositive() throws Exception {

        ChessGame chessGame = new ChessGame();

        GameData game = new GameData(
                0,
                null,
                null,
                "Test Game",
                chessGame
        );

        int gameID = gameDAO.createGame(game);

        GameData retrieved =
                gameDAO.getGame(gameID);

        assertNotNull(retrieved);

        assertEquals(
                "Test Game",
                retrieved.gameName()
        );
    }

    @Test
    @DisplayName("Create Game Negative")
    public void createGameNegative() {

        assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(null);
        });
    }

    @Test
    @DisplayName("Get Game Positive")
    public void getGamePositive() throws Exception {

        ChessGame chessGame = new ChessGame();

        int gameID = gameDAO.createGame(
                new GameData(
                        0,
                        null,
                        null,
                        "Lookup Game",
                        chessGame
                )
        );

        GameData retrieved =
                gameDAO.getGame(gameID);

        assertNotNull(retrieved);

        assertEquals(
                "Lookup Game",
                retrieved.gameName()
        );
    }

    @Test
    @DisplayName("Get Game Negative")
    public void getGameNegative() throws Exception {

        GameData game =
                gameDAO.getGame(99999);

        assertNull(game);
    }

    @Test
    @DisplayName("List Games Positive")
    public void listGamesPositive() throws Exception {

        ChessGame chessGame = new ChessGame();

        gameDAO.createGame(
                new GameData(
                        0,
                        null,
                        null,
                        "Game1",
                        chessGame
                )
        );

        gameDAO.createGame(
                new GameData(
                        0,
                        null,
                        null,
                        "Game2",
                        chessGame
                )
        );

        Collection<GameData> games =
                gameDAO.listGames();

        assertEquals(2, games.size());
    }

    @Test
    @DisplayName("List Games Negative")
    public void listGamesNegative() throws Exception {

        Collection<GameData> games =
                gameDAO.listGames();

        assertTrue(games.isEmpty());
    }

    @Test
    @DisplayName("Update Game Positive")
    public void updateGamePositive() throws Exception {

        ChessGame chessGame = new ChessGame();

        int gameID = gameDAO.createGame(
                new GameData(
                        0,
                        null,
                        null,
                        "Original",
                        chessGame
                )
        );

        GameData updated = new GameData(
                gameID,
                "whitePlayer",
                null,
                "Updated",
                chessGame
        );

        gameDAO.updateGame(updated);

        GameData retrieved =
                gameDAO.getGame(gameID);

        assertEquals(
                "whitePlayer",
                retrieved.whiteUsername()
        );

        assertEquals(
                "Updated",
                retrieved.gameName()
        );
    }

    @Test
    @DisplayName("Update Game Negative")
    public void updateGameNegative() {

        ChessGame chessGame = new ChessGame();

        assertThrows(DataAccessException.class, () -> {

            gameDAO.updateGame(
                    new GameData(
                            99999,
                            null,
                            null,
                            "Missing",
                            chessGame
                    )
            );
        });
    }

    @Test
    @DisplayName("Clear Positive")
    public void clearPositive() throws Exception {

        ChessGame chessGame = new ChessGame();

        gameDAO.createGame(
                new GameData(
                        0,
                        null,
                        null,
                        "Game1",
                        chessGame
                )
        );

        gameDAO.clear();

        assertTrue(
                gameDAO.listGames().isEmpty()
        );
    }
}