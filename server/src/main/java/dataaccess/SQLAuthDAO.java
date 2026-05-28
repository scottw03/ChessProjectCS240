package dataaccess;

import model.AuthData;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException {
        configureTable();
    }

    private void configureTable() throws DataAccessException {

        DatabaseManager.createDatabase();

        String[] statements = {
                """
                CREATE TABLE IF NOT EXISTS authTokens (
                    authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                    username VARCHAR(255) NOT NULL
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
    public void createAuth(AuthData auth)
            throws DataAccessException {

        if (auth == null) {
            throw new DataAccessException("bad request");
        }

        String sql = """
                INSERT INTO authTokens (authToken, username)
                VALUES (?, ?)
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, auth.authToken());
            ps.setString(2, auth.username());

            ps.executeUpdate();

        } catch (SQLException e) {

            if (e.getMessage().toLowerCase().contains("duplicate")) {
                throw new DataAccessException("already taken");
            }

            throw new DataAccessException("database error", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken)
            throws DataAccessException {

        String sql = """
                SELECT authToken, username
                FROM authTokens
                WHERE authToken=?
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, authToken);

            try (var rs = ps.executeQuery()) {

                if (rs.next()) {

                    return new AuthData(
                            rs.getString("authToken"),
                            rs.getString("username")
                    );
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("database error", e);
        }

        return null;
    }

    @Override
    public void deleteAuth(String authToken)
            throws DataAccessException {

        String sql = """
                DELETE FROM authTokens
                WHERE authToken=?
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, authToken);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("database error", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {

        String sql = "TRUNCATE authTokens";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("database error", e);
        }
    }
}