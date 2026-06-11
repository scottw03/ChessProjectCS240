package client;

import chess.ChessMove;
import model.AuthData;
import model.GameData;
import service.results.*;
import java.util.ArrayList;
import java.util.List;

public class ChessClient {

    private final ServerFacade server;
    private final WebSocketCommunicator ws;
    private String authToken;
    private String username;
    private Integer currentGameID;
    private String playerColor;
    private boolean observing;
    private final List<GameData> listedGames = new ArrayList<>();
    private State state = State.PRELOGIN;
    public enum State {
        PRELOGIN,
        POSTLOGIN,
        GAMEPLAY
    }

    public ChessClient(ServerFacade server) throws Exception {
        this.server = server;
        this.ws = new WebSocketCommunicator(server);
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
        ws.connect(
                authToken,
                game.gameID());
        currentGameID = game.gameID();
        playerColor = color.toUpperCase();
        observing = false;
        state = State.GAMEPLAY;
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
        ws.connect(
                authToken,
                game.gameID());
        currentGameID = game.gameID();
        playerColor = null;
        observing = true;
        state = State.GAMEPLAY;
    }

    public void leaveGame() throws Exception {
        ws.leave(
                authToken,
                currentGameID);
        currentGameID = null;
        playerColor = null;
        observing = false;
        state = State.POSTLOGIN;
    }

    public void resignGame() throws Exception {
        ws.resign(
                authToken,
                currentGameID);
    }

    public void makeMove(ChessMove move)
        throws Exception {
        ws.makeMove(
                authToken,
                currentGameID,
                move);
    }

    public List<GameData> getListedGames() {
        return listedGames;
    }
}
