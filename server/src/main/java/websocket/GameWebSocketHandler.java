package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import websocket.commands.ConnectCommand;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.ResignCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class GameWebSocketHandler {
    private final Gson gson = new Gson();
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final ConnectionManager connections =
            new ConnectionManager();

    public GameWebSocketHandler(
            GameDAO gameDAO,
            AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    private AuthData authenticate(
            String authToken) throws Exception {
        AuthData auth =
                authDAO.getAuth(authToken);
        if (auth == null) {
            throw new Exception(
                    "error: unauthorized");
        }
        return auth;
    }

    private void handleConnect(
            Session session,
            ConnectCommand command)
        throws Exception {
        AuthData auth =
                authenticate(
                        command.getAuthToken());
        if (auth == null) {
            session.getRemote().sendString(
                    gson.toJson(
                            new ErrorMessage(
                                    "Error: unauthorized")));
            return;
        }

        GameData game =
                gameDAO.getGame(
                        command.getGameID());
        if (game == null) {
            session.getRemote().sendString(
                    gson.toJson(
                            new ErrorMessage(
                                    "Error: game not found")));
            return;
        }

        String username =
                auth.username();
        connections.addConnection(
                game.gameID(),
                username,
                session);
        session.getRemote().sendString(
                gson.toJson(
                        new LoadGameMessage(
                                game.game())));
        String role =
                determineRole(
                        game,
                        username);
        connections.broadcastExcept(
                game.gameID(),
                username,
                new NotificationMessage(
                        username + " connected as "
                        + role));
    }

    private String determineRole(
            GameData game,
            String username) {
        if (username.equals(
                game.whiteUsername())) {
            return "white";
        }
        if (username.equals(
                game.blackUsername())) {
            return "black";
        }
        return "an observer";
    }

    public void onMessage(
            Session session,
            String message)
            throws Exception {

        UserGameCommand command =
                gson.fromJson(
                        message,
                        UserGameCommand.class);

        switch (command.getCommandType()) {

            case CONNECT ->
                    handleConnect(
                            session,
                            gson.fromJson(
                                    message,
                                    ConnectCommand.class));

            case MAKE_MOVE ->
                    handleMakeMove(
                            session,
                            gson.fromJson(
                                    message,
                                    MakeMoveCommand.class));

            case LEAVE ->
                    handleLeave(
                            session,
                            gson.fromJson(
                                    message,
                                    LeaveCommand.class));

            case RESIGN ->
                    handleResign(
                            session,
                            gson.fromJson(
                                    message,
                                    ResignCommand.class));
        }
    }

    private void handleMakeMove(
            Session session,
            MakeMoveCommand command)
        throws Exception {
        AuthData auth =
                authenticate(
                        command.getAuthToken());
        GameData game =
                gameDAO.getGame(
                        command.getGameID());
        String username =
                auth.username();
        boolean whitePlayer =
                username.equals(
                        game.whiteUsername());
        boolean blackPlayer =
                username.equals(
                        game.blackUsername());
        if (!whitePlayer && !blackPlayer) {
            connections.sendToUser(
                    game.gameID(),
                    username,
                    new ErrorMessage(
                            "Error: observers can't move"));
            return;
        }
        ChessGame chessGame =
                game.game();
        if (whitePlayer && chessGame.getTeamTurn()
        != ChessGame.TeamColor.WHITE) {
            connections.sendToUser(
                    game.gameID(),
                    username,
                    new ErrorMessage(
                            "Error: it's not your turn"));
            return;
        }
        if (blackPlayer && chessGame.getTeamTurn()
        != ChessGame.TeamColor.BLACK) {
            connections.sendToUser(
                    game.gameID(),
                    username,
                    new ErrorMessage(
                            "Error: it's not your turn"));
            return;
        }
        try {
            chessGame.makeMove(
                    command.getMove());
        } catch (Exception ex) {
            connections.sendToUser(
                    game.gameID(),
                    username,
                    new ErrorMessage(
                            "Error: invalid move"));
            return;
        }
        GameData updatedGame =
                new GameData(
                        game.gameID(),
                        game.whiteUsername(),
                        game.blackUsername(),
                        game.gameName(),
                        chessGame);
        gameDAO.updateGame(updatedGame);
        connections.broadcast(
                game.gameID(),
                new LoadGameMessage(
                        chessGame));
        connections.broadcastExcept(
                game.gameID(),
                username,
                new NotificationMessage(
                        username
                        + " moved "
                        + moveToString(
                                command.getMove())));
        ChessGame.TeamColor currentTurn =
                chessGame.getTeamTurn();
        if (chessGame.isInCheckmate(
                currentTurn)) {
            connections.broadcast(
                    game.gameID(),
                    new NotificationMessage(
                            currentTurn + " is in checkmate"));
            chessGame.setGameOver(true);
            gameDAO.updateGame(
                    updatedGame);
        }
        else if (chessGame.isInStalemate(
                currentTurn)) {
            connections.broadcast(
                    game.gameID(),
                    new NotificationMessage(
                            currentTurn
                            + " is in stalemate"));
            chessGame.setGameOver(true);
            gameDAO.updateGame(
                    updatedGame);
        }
        else if (chessGame.isInCheck(
                currentTurn)) {
            connections.broadcast(
                    game.gameID(),
                    new NotificationMessage(
                            currentTurn
                            + " is in check"));
        }
    }

    private String moveToString(
            ChessMove move) {
        return positionToString(
                move.getStartPosition())
                + " to "
                + positionToString(
                move.getEndPosition());
    }

    private String positionToString(
            ChessPosition pos) {
        char file =
                (char) ('a'
                        + pos.getColumn()
                        - 1);
        return file
                + String.valueOf(
                pos.getRow());
    }

    private void handleLeave(
            Session session,
            LeaveCommand command)
        throws Exception {
        AuthData auth =
                authenticate(
                        command.getAuthToken());
        GameData game =
                gameDAO.getGame(
                        command.getGameID());
        String username =
                auth.username();
        boolean playerLeft = false;
        String white =
                game.whiteUsername();
        String black =
                game.blackUsername();
        if (username.equals(white)) {
            white = null;
            playerLeft = true;
        }
        if (username.equals(black)) {
            black = null;
            playerLeft = true;
        }
        if (playerLeft) {
            GameData updated =
                    new GameData(
                            game.gameID(),
                            white,
                            black,
                            game.gameName(),
                            game.game());
            gameDAO.updateGame(updated);
        }
        connections.removeConnection(
                game.gameID(),
                username);
        connections.broadcast(
                game.gameID(),
                new NotificationMessage(
                        username + " left the game"));
    }

    private void handleResign(
            Session session,
            ResignCommand command)
        throws Exception {
        AuthData auth =
                authenticate(
                        command.getAuthToken());
        GameData game =
                gameDAO.getGame(
                        command.getGameID());
        ChessGame chessGame =
                game.game();
        if (chessGame.isGameOver()) {
            connections.sendToUser(
                    game.gameID(),
                    auth.username(),
                    new ErrorMessage(
                            "Error: game already over"));
            return;
        }
        chessGame.setGameOver(true);
        gameDAO.updateGame(
                new GameData(
                        game.gameID(),
                        game.whiteUsername(),
                        game.blackUsername(),
                        game.gameName(),
                        chessGame));
        connections.broadcast(
                game.gameID(),
                new NotificationMessage(
                        auth.username()
                        + " resigned"));
    }
}
