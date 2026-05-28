package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import service.requests.RegisterRequest;
import service.results.RegisterResult;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.results.LoginResult;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }
    public RegisterResult register(RegisterRequest request)
            throws Exception {
        if (request.username() == null || request.password() == null || request.email() == null ||
                request.username().isBlank() || request.password().isBlank() || request.email().isBlank()) {
            throw new Exception("bad request");
        }
        if (userDAO.getUser(request.username()) != null) {
            throw new Exception("already taken");
        }
        UserData user = new UserData(
                request.username(), request.password(), request.email()
        );
        userDAO.createUser(user);
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, request.username());
        authDAO.createAuth(auth);
        return new RegisterResult(request.username(), token);
    }
    public LoginResult login(LoginRequest request) throws Exception {
        if (request.username() == null || request.password() == null ||
                request.username().isBlank() || request.password().isBlank()) {
            throw new Exception("bad request");
        }
        UserData user = userDAO.getUser(request.username());
        if (user == null) {
            throw new Exception("unauthorized");
        }
        if (!BCrypt.checkpw(request.password(), user.password())) {
            throw new Exception("unauthorized");
        }
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, request.username());
        authDAO.createAuth(authData);
        return new LoginResult(request.username(), authToken);
    }
    public void logout(LogoutRequest request) throws Exception {
        if (request.authToken() == null) {
            throw new Exception("unauthorized");
        }
        AuthData auth = authDAO.getAuth(request.authToken());
        if (auth == null) {
            throw new Exception("unauthorized");
        }
        authDAO.deleteAuth(request.authToken());
    }
}
