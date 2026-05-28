package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import chess.ChessGame;
import service.requests.*;
import service.results.*;

import java.util.Collection;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }
    public CreateGameResult createGame(CreateGameRequest request) throws Exception {
        if (request == null) {
            throw new Exception("bad request");
        }
        AuthData auth = authDAO.getAuth(request.authToken());
        if (auth == null) {
            throw new Exception("unauthorized");
        }
        if (request.gameName() == null || request.gameName().isBlank()) {
            throw new Exception("bad request");
        }
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(
                0,
                null,
                null,
                request.gameName(),
                game
        );
        int gameID = gameDAO.createGame(gameData);
        return new CreateGameResult(gameID);
    }
    public ListGamesResult listGames(
            ListGamesRequest request) throws Exception {
        if (request == null) {
            throw new Exception("bad request");
        }
        AuthData auth = authDAO.getAuth(request.authToken());
        if (auth == null) {
            throw new Exception("unauthorized");
        }
        Collection<GameData> games = gameDAO.listGames();
        return new ListGamesResult(games);
    }
    public void joinGame(JoinGameRequest request) throws Exception {
        if (request == null) {
            throw new Exception("bad request");
        }
        AuthData auth = authDAO.getAuth(request.authToken());
        if (auth == null) {
            throw new Exception("unauthorized");
        }
        if (request.gameID() == null) {
            throw new Exception("bad request");
        }
        String color = request.playerColor();
        if (color == null || color.isBlank()) {
            throw new Exception("bad request");
        }
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            throw new Exception("bad request");
        }
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new Exception("bad request");
        }
        if (color.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new Exception("already taken");
            }
            game = new GameData(
                    game.gameID(),
                    auth.username(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );
        }
        else if (color.equals("BLACK")) {
            if (game.blackUsername() != null) {
                throw new Exception("already taken");
            }
            game = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    auth.username(),
                    game.gameName(),
                    game.game()
            );
        }
        gameDAO.updateGame(game);
    }
}