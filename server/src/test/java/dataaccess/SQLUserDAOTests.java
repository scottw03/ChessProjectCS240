package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTests {

    private SQLUserDAO userDAO;

    @BeforeEach
    public void setUp() throws Exception {
        userDAO = new SQLUserDAO();
        userDAO.clear();
    }

    @Test
    @DisplayName("Create User Positive")
    public void createUserPositive() throws Exception {

        UserData user = new UserData(
                "player1",
                "password123",
                "player1@email.com"
        );

        userDAO.createUser(user);

        UserData retrieved = userDAO.getUser("player1");

        assertNotNull(retrieved);

        assertEquals(user.username(), retrieved.username());
        assertEquals(user.email(), retrieved.email());

        assertNotEquals(user.password(), retrieved.password());
    }

    @Test
    @DisplayName("Create User Negative Duplicate")
    public void createUserNegativeDuplicate() throws Exception {

        UserData user = new UserData(
                "duplicate",
                "password",
                "email@test.com"
        );

        userDAO.createUser(user);

        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(user);
        });
    }

    @Test
    @DisplayName("Get User Positive")
    public void getUserPositive() throws Exception {

        UserData user = new UserData(
                "lookup",
                "password",
                "lookup@test.com"
        );

        userDAO.createUser(user);

        UserData retrieved = userDAO.getUser("lookup");

        assertNotNull(retrieved);

        assertEquals("lookup", retrieved.username());
        assertEquals("lookup@test.com", retrieved.email());
    }

    @Test
    @DisplayName("Get User Negative")
    public void getUserNegative() throws Exception {

        UserData retrieved = userDAO.getUser("doesNotExist");

        assertNull(retrieved);
    }

    @Test
    @DisplayName("Clear Positive")
    public void clearPositive() throws Exception {

        userDAO.createUser(
                new UserData("u1", "p1", "e1")
        );

        userDAO.createUser(
                new UserData("u2", "p2", "e2")
        );

        userDAO.clear();

        assertNull(userDAO.getUser("u1"));
        assertNull(userDAO.getUser("u2"));
    }
}