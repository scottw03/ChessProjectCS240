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

    }

    public String login(
            String username,
            String password)
        throws Exception {

    }

    public String logout()
        throws Exception {

    }

    public String createGame(
            String gameName)
        throws Exception {

    }

    public List<GameData> listGames()
        throws Exception {

    }

    public String joinGame(
            int gameNumber,
            String color)
        throws Exception {

    }

    public List<GameData> getListedGames() {
        return listedGames;
    }
}
