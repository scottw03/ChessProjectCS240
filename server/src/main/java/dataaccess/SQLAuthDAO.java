package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public class SQLAuthDAO extends SQLDAO implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException {
        configureTable();
    }

    private void configureTable()
            throws DataAccessException {

        DatabaseManager.createDatabase();

        String[] statements = {
                """
                CREATE TABLE IF NOT EXISTS authTokens (
                    authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                    username VARCHAR(255) NOT NULL
                )
                """
        };

        configureDatabase(statements);
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

        try {
                executeUpdate(
                        sql,
                        auth.authToken(),
                        auth.username()
                );

        } catch (DataAccessException e) {

            if (e.getMessage()
                    .toLowerCase()
                    .contains("duplicate")) {
                throw new DataAccessException("already taken");
            }

            throw e;
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

            try (var rs = executeQuery(ps, authToken)) {

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

        executeUpdate(sql, authToken);
    }

    @Override
    public void clear()
            throws DataAccessException {

        executeUpdate("TRUNCATE authTokens");
    }
}