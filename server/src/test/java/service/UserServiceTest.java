package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.*;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();

        userService = new UserService(userDAO, authDAO);
    }

    // ---------------- REGISTER TESTS ----------------

    @Test
    @DisplayName("Register Positive")
    public void registerPositive() throws Exception {

        RegisterRequest request =
                new RegisterRequest("player1", "password", "player@email.com");

        RegisterResult result = userService.register(request);

        assertNotNull(result);
        assertEquals("player1", result.username());
        assertNotNull(result.authToken());

        assertNotNull(userDAO.getUser("player1"));
        assertNotNull(authDAO.getAuth(result.authToken()));
    }

    @Test
    @DisplayName("Register Negative Duplicate User")
    public void registerNegativeDuplicate() throws Exception {

        RegisterRequest request =
                new RegisterRequest("player1", "password", "player@email.com");

        userService.register(request);

        Exception ex = assertThrows(Exception.class, () ->
                userService.register(request));

        assertEquals("already taken", ex.getMessage());
    }

    // ---------------- LOGIN TESTS ----------------

    @Test
    @DisplayName("Login Positive")
    public void loginPositive() throws Exception {

        RegisterRequest registerRequest =
                new RegisterRequest("player1", "password", "email@email.com");

        userService.register(registerRequest);

        LoginRequest loginRequest =
                new LoginRequest("player1", "password");

        LoginResult result = userService.login(loginRequest);

        assertNotNull(result);
        assertEquals("player1", result.username());
        assertNotNull(result.authToken());

        assertNotNull(authDAO.getAuth(result.authToken()));
    }

    @Test
    @DisplayName("Login Negative Wrong Password")
    public void loginNegativeWrongPassword() throws Exception {

        RegisterRequest registerRequest =
                new RegisterRequest("player1", "password", "email@email.com");

        userService.register(registerRequest);

        LoginRequest badLogin =
                new LoginRequest("player1", "wrongpassword");

        Exception ex = assertThrows(Exception.class, () ->
                userService.login(badLogin));

        assertEquals("unauthorized", ex.getMessage());
    }

    // ---------------- LOGOUT TESTS ----------------

    @Test
    @DisplayName("Logout Positive")
    public void logoutPositive() throws Exception {

        RegisterRequest registerRequest =
                new RegisterRequest("player1", "password", "email@email.com");

        RegisterResult registerResult =
                userService.register(registerRequest);

        String token = registerResult.authToken();

        assertNotNull(authDAO.getAuth(token));

        LogoutRequest logoutRequest =
                new LogoutRequest(token);

        userService.logout(logoutRequest);

        assertNull(authDAO.getAuth(token));
    }

    @Test
    @DisplayName("Logout Negative Invalid Token")
    public void logoutNegativeInvalidToken() {

        LogoutRequest request =
                new LogoutRequest("bad-token");

        Exception ex = assertThrows(Exception.class, () ->
                userService.logout(request));

        assertEquals("unauthorized", ex.getMessage());
    }
    @Test
    @DisplayName("Login Generates Unique Tokens")
    public void loginUniqueTokens() throws Exception {

        RegisterRequest registerRequest =
                new RegisterRequest("player1", "password", "email@email.com");

        RegisterResult registerResult =
                userService.register(registerRequest);

        LoginResult login1 =
                userService.login(new LoginRequest("player1", "password"));

        LoginResult login2 =
                userService.login(new LoginRequest("player1", "password"));

        assertNotEquals(registerResult.authToken(), login1.authToken());
        assertNotEquals(login1.authToken(), login2.authToken());
    }
}