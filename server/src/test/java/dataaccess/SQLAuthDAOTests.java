package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTests {

    private SQLAuthDAO authDAO;

    @BeforeEach
    public void setUp() throws Exception {
        authDAO = new SQLAuthDAO();
        authDAO.clear();
    }

    @Test
    @DisplayName("Create Auth Positive")
    public void createAuthPositive() throws Exception {

        AuthData auth = new AuthData(
                "token123",
                "player1"
        );

        authDAO.createAuth(auth);

        AuthData retrieved =
                authDAO.getAuth("token123");

        assertNotNull(retrieved);

        assertEquals(
                auth.authToken(),
                retrieved.authToken()
        );

        assertEquals(
                auth.username(),
                retrieved.username()
        );
    }

    @Test
    @DisplayName("Create Auth Negative Duplicate")
    public void createAuthNegativeDuplicate()
            throws Exception {

        AuthData auth = new AuthData(
                "duplicateToken",
                "player1"
        );

        authDAO.createAuth(auth);

        assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth(auth);
        });
    }

    @Test
    @DisplayName("Get Auth Positive")
    public void getAuthPositive() throws Exception {

        AuthData auth = new AuthData(
                "lookupToken",
                "player2"
        );

        authDAO.createAuth(auth);

        AuthData retrieved =
                authDAO.getAuth("lookupToken");

        assertNotNull(retrieved);

        assertEquals(
                "lookupToken",
                retrieved.authToken()
        );

        assertEquals(
                "player2",
                retrieved.username()
        );
    }

    @Test
    @DisplayName("Get Auth Negative")
    public void getAuthNegative() throws Exception {

        AuthData retrieved =
                authDAO.getAuth("missingToken");

        assertNull(retrieved);
    }

    @Test
    @DisplayName("Delete Auth Positive")
    public void deleteAuthPositive() throws Exception {

        AuthData auth = new AuthData(
                "deleteToken",
                "player3"
        );

        authDAO.createAuth(auth);

        authDAO.deleteAuth("deleteToken");

        assertNull(
                authDAO.getAuth("deleteToken")
        );
    }

    @Test
    @DisplayName("Delete Auth Negative")
    public void deleteAuthNegative() throws Exception {

        authDAO.deleteAuth("doesNotExist");

        assertNull(
                authDAO.getAuth("doesNotExist")
        );
    }

    @Test
    @DisplayName("Clear Positive")
    public void clearPositive() throws Exception {

        authDAO.createAuth(
                new AuthData("t1", "u1")
        );

        authDAO.createAuth(
                new AuthData("t2", "u2")
        );

        authDAO.clear();

        assertNull(authDAO.getAuth("t1"));
        assertNull(authDAO.getAuth("t2"));
    }
}