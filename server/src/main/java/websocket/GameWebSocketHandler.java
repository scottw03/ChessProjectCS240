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
                authDAO.getAuth(
                        command.getAuthToken());
        if (auth == null) {
            connections.sendToSession(
                    session,
                    new ErrorMessage(
                            "Error: unauthorized"));
            return;
        }

        GameData game =
                gameDAO.getGame(
                        command.getGameID());
        if (game == null) {
            connections.sendToSession(
                    session,
                            new ErrorMessage(
                                    "Error: game not found"));
            return;
        }

        String username =
                auth.username();
        connections.addConnection(
                game.gameID(),
                username,
                session);
        connections.sendToSession(
                session,
                        new LoadGameMessage(
                                game.game()));
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
        if (game.whiteUsername() != null &&
        game.whiteUsername().equals(username)) {
            return "white";
        }
        if (game.blackUsername() != null &&
        game.blackUsername().equals(username)) {
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

    private AuthData validateMoveAuth(
            Session session,
            MakeMoveCommand command)
        throws Exception {
        AuthData auth =
                authDAO.getAuth(command.getAuthToken());
        if (auth == null) {
            connections.sendToSession(
                    session,
                    new ErrorMessage(
                            "Error: unauthorized"));
        }
        return auth;
    }

    private boolean validatePlayer(
            GameData game,
            String username) {
        return username.equals(
                game.whiteUsername()) ||
                username.equals(
                        game.blackUsername());
    }

    private boolean validateTurn(
            ChessGame game,
            String username,
            GameData gameData) {
        if (username.equals(gameData.whiteUsername())) {
            return game.getTeamTurn()
                    == ChessGame.TeamColor.WHITE;
        }
        if (username.equals(gameData.blackUsername())) {
            return game.getTeamTurn()
                    == ChessGame.TeamColor.BLACK;
        }
        return false;
    }

    private boolean executeMove(
            ChessGame chessGame,
            ChessMove move,
            int gameID,
            String username)
        throws Exception {
        try {
            chessGame.makeMove(move);
            return true;
        } catch (Exception ex) {
            connections.sendToUser(
                    gameID,
                    username,
                    new ErrorMessage(
                            "Error: invalid move"));
            return false;
        }
    }

    private GameData saveUpdatedGame(
            GameData game,
            ChessGame chessGame)
        throws Exception {
        GameData updatedGame =
                new GameData(
                        game.gameID(),
                        game.whiteUsername(),
                        game.blackUsername(),
                        game.gameName(),
                        chessGame);
        gameDAO.updateGame(updatedGame);
        return updatedGame;
    }

    private void broadcastBoardUpdate(
            GameData game,
            ChessGame chessGame)
        throws Exception {
        connections.broadcast(
                game.gameID(),
                new LoadGameMessage(
                        chessGame));
    }

    private String getPlayerUsername(
            GameData game,
            ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            return game.whiteUsername();
        }
        return game.blackUsername();
    }


    private void processGameStatus(
            GameData game,
            ChessGame chessGame)
        throws Exception {
        ChessGame.TeamColor currentTurn =
                chessGame.getTeamTurn();
        String playerName =
                getPlayerUsername(
                        game,
                        currentTurn);
        if (chessGame.isInCheckmate(currentTurn)) {
            connections.broadcast(
                    game.gameID(),
                    new NotificationMessage(
                            playerName +
                                    " is in checkmate"));
            chessGame.setGameOver(true);
            gameDAO.updateGame(
                    new GameData(
                            game.gameID(),
                            game.whiteUsername(),
                            game.blackUsername(),
                            game.gameName(),
                            chessGame));
        }
        else if (chessGame.isInStalemate(currentTurn)) {
            connections.broadcast(
                    game.gameID(),
                    new NotificationMessage(
                            playerName +
                                    " is in stalemate"));
            chessGame.setGameOver(true);
            gameDAO.updateGame(
                    new GameData(
                            game.gameID(),
                            game.whiteUsername(),
                            game.blackUsername(),
                            game.gameName(),
                            chessGame));
        }
        else if (chessGame.isInCheck(currentTurn)) {
            connections.broadcast(
                    game.gameID(),
                    new NotificationMessage(
                            playerName +
                                    " is in check"));
        }
    }

    private void handleMakeMove(
            Session session,
            MakeMoveCommand command)
        throws Exception {
        AuthData auth =
                validateMoveAuth(
                        session,
                        command);
        if (auth == null) {
            return;
        }
        GameData game =
                gameDAO.getGame(
                        command.getGameID());
        if (game == null) {
            connections.sendToSession(
                    session,
                    new ErrorMessage(
                            "Error: game not found"));
            return;
        }
        String username =
                auth.username();
        if (!validatePlayer(game,username)) {
            connections.sendToUser(
                    game.gameID(),
                    username,
                    new ErrorMessage(
                            "Error: observers can't move"));
            return;
        }
        ChessGame chessGame = game.game();
        if (!validateTurn(chessGame, username, game)) {
            connections.sendToUser(
                    game.gameID(),
                    username,
                    new ErrorMessage(
                            "Error: it's not your turn"));
            return;
        }
        if (!executeMove(
                chessGame,
                command.getMove(),
                game.gameID(),
                username)) {
            return;
        }
        GameData updatedGame =
                saveUpdatedGame(game, chessGame);
        broadcastBoardUpdate(
                updatedGame,
                chessGame);
        broadcastMove(
                updatedGame,
                username,
                command.getMove());
        processGameStatus(
                updatedGame,
                chessGame);
    }

    private void broadcastMove(
            GameData game,
            String username,
            ChessMove move)
        throws Exception {
        connections.broadcastExcept(
                game.gameID(),
                username,
                new NotificationMessage(
                        username + " moved "
                        + moveToString(move)));
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
        session.close();
    }

    private void handleResign(
            Session session,
            ResignCommand command)
        throws Exception {
        AuthData auth =
                authenticate(
                        command.getAuthToken());
        String username =
                auth.username();
        GameData game =
                gameDAO.getGame(
                        command.getGameID());
        boolean isWhite =
                game.whiteUsername() != null &&
                        game.whiteUsername().equals(username);
        boolean isBlack =
                game.blackUsername() != null &&
                        game.blackUsername().equals(username);
        if (!isWhite && !isBlack) {
            connections.sendToSession(
                    session,
                    new ErrorMessage(
                            "Error: observers cannot resign"));
            return;
        }
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
