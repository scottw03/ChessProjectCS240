package dataaccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLDAO {

    protected void configureDatabase(String[] statements)
            throws DataAccessException {

        DatabaseManager.createDatabase();

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
    protected int executeUpdate(
            String sql,
            Object... params
    ) throws DataAccessException {

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            setParameters(ps, params);
            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException(
                    "database error",
                    e
            );
        }
    }
    protected ResultSet executeQuery(
            PreparedStatement ps,
            Object... params
    ) throws SQLException {

        setParameters(ps, params);
        return ps.executeQuery();
    }
    protected void setParameters(
            PreparedStatement ps,
            Object... params
    ) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}