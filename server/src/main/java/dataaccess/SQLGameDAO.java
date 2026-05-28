package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class SQLGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    public SQLGameDAO() throws DataAccessException {
        configureTable();
    }

    private void configureTable() throws DataAccessException {

        DatabaseManager.createDatabase();

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

        try (var conn = DatabaseManager.getConnection()) {

            for (String statement : statements) {

                try (PreparedStatement ps =
                             conn.prepareStatement(statement)) {

                    ps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Unable to configure database",
                    e
            );
        }
    }

    @Override
    public int createGame(GameData game)
            throws DataAccessException {

        if (game == null) {
            throw new DataAccessException("bad request");
        }

        String gameJson =
                gson.toJson(game.game());

        String sql = """
                INSERT INTO games
                (whiteUsername, blackUsername, gameName, game)
                VALUES (?, ?, ?, ?)
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(
                     sql,
                     PreparedStatement.RETURN_GENERATED_KEYS
             )) {

            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, gameJson);

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

            ps.setInt(1, gameID);

            try (var rs = ps.executeQuery()) {

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

                GameData game = new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        chessGame
                );

                games.add(game);
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

        String gameJson =
                gson.toJson(game.game());

        String sql = """
                UPDATE games
                SET whiteUsername=?,
                    blackUsername=?,
                    gameName=?,
                    game=?
                WHERE gameID=?
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, gameJson);
            ps.setInt(5, game.gameID());

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated == 0) {
                throw new DataAccessException("bad request");
            }

        } catch (SQLException e) {
            throw new DataAccessException("database error", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {

        String sql = "TRUNCATE games";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("database error", e);
        }
    }
}