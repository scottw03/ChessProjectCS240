package client;

import model.AuthData;
import model.GameData;
import service.results.*;
import java.util.ArrayList;
import java.util.List;

public class ChessClient {

    private final ServerFacade server;
    private String authToken;
    private String username;
    private final List<GameData> listedGames = new ArrayList<>();
    private State state = State.PRELOGIN;
    public enum State {
        PRELOGIN,
        POSTLOGIN
    }

    public ChessClient(ServerFacade server) {
        this.server = server;
    }

    public State getState() {
        return state;
    }

    public String register(
            String username,
            String password,
            String email)
        throws Exception {
        if (username == null || username.isBlank()) {
            throw new Exception(
                    "Please enter a username.");
        }
        if (password == null || password.isBlank()) {
            throw new Exception(
                    "Please enter a password.");
        }
        if (email == null || email.isBlank()) {
            throw new Exception(
                    "Please enter an email.");
        }
        AuthData auth =
                server.register(
                        username,
                        password,
                        email);
        this.username = auth.username();
        this.authToken = auth.authToken();
        this.state = State.POSTLOGIN;
        return "Successfully registered.";
    }

    public String login(
            String username,
            String password)
        throws Exception {
        if (username == null || username.isBlank()) {
            throw new Exception(
                    "Please enter a username.");
        }
        if (password == null || password.isBlank()) {
            throw new Exception(
                    "Please enter a password.");
        }

        AuthData auth =
            server.login(
                    username,
                    password);
        this.username = auth.username();
        this.authToken = auth.authToken();
        this.state = State.POSTLOGIN;
        return "Successfully logged in.";
    }

    public String logout()
        throws Exception {
        server.logout(authToken);
        authToken = null;
        username = null;
        listedGames.clear();
        state = State.PRELOGIN;
        return "Logged out.";
    }

    public String createGame(
            String gameName)
        throws Exception {
        CreateGameResult result =
                server.createGame(
                        gameName,
                        authToken);
        return "Game created. ID = "
                + result.gameID();
    }

    public List<GameData> listGames()
        throws Exception {
        ListGamesResult result =
                server.listGames(authToken);
        listedGames.clear();
        listedGames.addAll(result.games());
        return new ArrayList<>(result.games());
    }

    public String joinGame(
            String color,
            int gameNumber)
        throws Exception {
        if (listedGames.isEmpty()) {
            throw new Exception(
                    "Please use 'list' before joining a game.");
        }
        if (gameNumber < 1 ||
            gameNumber > listedGames.size()) {
            throw new Exception(
                    "Invalid game number");
        }
        GameData game =
                listedGames.get(gameNumber - 1);
        server.joinGame(
                color,
                game.gameID(),
                authToken);
        return "Joined game.";
    }

    public void observeGame(
            int gameNumber)
        throws Exception {
        if (listedGames.isEmpty()) {
            throw new Exception(
                    "Please use 'list' first.");
        }
        if (gameNumber < 1 ||
        gameNumber > listedGames.size()) {
            throw new Exception(
                    "Invalid game number.");
        }
        GameData game = listedGames.get(gameNumber - 1);
    }

    public List<GameData> getListedGames() {
        return listedGames;
    }
}
