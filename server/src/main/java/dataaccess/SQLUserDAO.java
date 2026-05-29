package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class SQLUserDAO extends SQLDAO implements UserDAO {

    public SQLUserDAO() throws DataAccessException {
        configureTable();
    }

    private void configureTable() throws DataAccessException {

        String[] statements = {
                """
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) NOT NULL PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL
                )
                """
        };

        configureDatabase(statements);
    }

    @Override
    public void createUser(UserData user)
            throws DataAccessException {

        if (user == null) {
            throw new DataAccessException("bad request");
        }

        String hashedPassword =
                BCrypt.hashpw(
                        user.password(),
                        BCrypt.gensalt()
                );

        String sql = """
                INSERT INTO users (username, password, email)
                VALUES (?, ?, ?)
                """;

        try {
            executeUpdate(
                    sql,
                    user.username(),
                    hashedPassword,
                    user.email()
            );

        } catch (DataAccessException e) {

            if (e.getMessage().toLowerCase().contains("duplicate")) {
                throw new DataAccessException("already taken");
            }

            throw e;
        }
    }

    @Override
    public UserData getUser(String username)
            throws DataAccessException {

        String sql = """
                SELECT username, password, email
                FROM users
                WHERE username=?
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            try (var rs = executeQuery(ps, username)) {

                if (rs.next()) {

                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("database error", e);
        }

        return null;
    }

    @Override
    public void clear()
            throws DataAccessException {

        executeUpdate("TRUNCATE users");
    }
}
