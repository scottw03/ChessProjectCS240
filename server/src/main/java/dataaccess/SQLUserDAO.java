package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws DataAccessException {
        configureTable();
    }

    private void configureTable() throws DataAccessException {
        DatabaseManager.createDatabase();

        String[] statements = {
                """
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) NOT NULL PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL
                )
                """
        };

        try (var conn = DatabaseManager.getConnection()) {
            for (String statement : statements) {
                try (PreparedStatement ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to configure database", e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

        if (user == null) {
            throw new DataAccessException("bad request");
        }

        String hashedPassword =
                BCrypt.hashpw(user.password(), BCrypt.gensalt());

        String sql = """
                INSERT INTO users (username, password, email)
                VALUES (?, ?, ?)
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.username());
            ps.setString(2, hashedPassword);
            ps.setString(3, user.email());

            ps.executeUpdate();

        } catch (SQLException e) {

            if (e.getMessage().toLowerCase().contains("duplicate")) {
                throw new DataAccessException("already taken");
            }

            throw new DataAccessException("database error", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {

        String sql = """
                SELECT username, password, email
                FROM users
                WHERE username=?
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (var rs = ps.executeQuery()) {

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
    public void clear() throws DataAccessException {

        String sql = "TRUNCATE users";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("database error", e);
        }
    }
}
