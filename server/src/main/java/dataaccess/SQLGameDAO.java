package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class SQLGameDAO extends SQLDAO implements GameDAO {

    private final Gson gson = new Gson();

    public SQLGameDAO() throws DataAccessException {
        configureTable();
    }

    private void configureTable()
            throws DataAccessException {

        String[] statements = {
                """
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255) NOT NULL,
                    game TEXT NOT NULL
                )
                """
        };

        configureDatabase(statements);
    }

    @Override
    public int createGame(GameData game)
            throws DataAccessException {

        if (game == null) {
            throw new DataAccessException("bad request");
        }

        String sql = """
                INSERT INTO games
                (whiteUsername, blackUsername, gameName, game)
                VALUES (?, ?, ?, ?)
                """;

        String gameJson =
                gson.toJson(game.game());


        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(
                     sql,
                     PreparedStatement.RETURN_GENERATED_KEYS
             )) {

            setParameters(
                    ps,
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName(),
                    gameJson
            );

            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("database error", e);
        }

        throw new DataAccessException(
                "Unable to retrieve game ID"
        );
    }

    @Override
    public GameData getGame(int gameID)
            throws DataAccessException {

        String sql = """
                SELECT *
                FROM games
                WHERE gameID=?
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            try (var rs = executeQuery(ps, gameID)) {

                if (rs.next()) {

                    ChessGame chessGame =
                            gson.fromJson(
                                    rs.getString("game"),
                                    ChessGame.class
                            );

                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            chessGame
                    );
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("database error", e);
        }

        return null;
    }

    @Override
    public Collection<GameData> listGames()
            throws DataAccessException {

        Collection<GameData> games =
                new ArrayList<>();

        String sql = "SELECT * FROM games";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            while (rs.next()) {

                ChessGame chessGame =
                        gson.fromJson(
                                rs.getString("game"),
                                ChessGame.class
                        );

                games.add(
                        new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        chessGame
                        )
                );
            }

        } catch (SQLException e) {
            throw new DataAccessException("database error", e);
        }

        return games;
    }

    @Override
    public void updateGame(GameData game)
            throws DataAccessException {

        if (game == null) {
            throw new DataAccessException("bad request");
        }

        String sql = """
                UPDATE games
                SET whiteUsername=?,
                    blackUsername=?,
                    gameName=?,
                    game=?
                WHERE gameID=?
                """;


        String gameJson =
                gson.toJson(game.game());

        int rowsUpdated =
                executeUpdate(
                        sql,
                        game.whiteUsername(),
                        game.blackUsername(),
                        game.gameName(),
                        gameJson,
                        game.gameID()
                );

            if (rowsUpdated == 0) {
                throw new DataAccessException("bad request");
            }
    }

    @Override
    public void clear()
            throws DataAccessException {

        executeUpdate("TRUNCATE games");
    }
}