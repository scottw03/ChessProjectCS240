package client;

import java.util.Scanner;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import ui.BoardRenderer;
import websocket.messages.LoadGameMessage;

public class Repl {
    private final ChessClient client;
    private final Scanner scanner = new Scanner(System.in);

    public Repl(ChessClient client) {
        this.client = client;
    }

    public void run() {
        System.out.println("Welcome to 240 Chess!");
        boolean running = true;
        while (running) {
            try {
                printPrompt();
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("quit")) {
                    running = false;
                }
                else {
                    executeCommand(input);
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
        System.out.println("Goodbye.");
    }

    private void printPrompt() {
        if (client.getState() ==
        ChessClient.State.PRELOGIN) {
            System.out.print("[LOGGED OUT] >>> ");
        }
        else {
            System.out.print("[LOGGED IN] >>> ");
        }
    }

    private void executeCommand(String input)
        throws Exception {
        String[] tokens =
                input.split("\\s+");
        String command =
                tokens[0].toLowerCase();
        switch (client.getState()) {
            case PRELOGIN ->
                executePrelogin(command);
            case POSTLOGIN ->
                executePostlogin(command);
            case GAMEPLAY ->
                executeGameplay(command);
        }
    }

    private void executePrelogin(String command)
        throws Exception {
        switch (command) {
            case "help" -> printPreloginHelp();
            case "register" -> register();
            case "login" -> login();
            default ->
                System.out.println(
                        "Unknown command. Type 'help'.");
        }
    }

    private void printPreloginHelp() {
        System.out.println("""
                register - to create an account
                login - to play chess
                quit - playing chess
                help - with possible commands
                """);
    }

    private void register() throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        String result =
                client.register(
                        username,
                        password,
                        email);
        System.out.println(result);
    }

    private void login() throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        String result = client.login(
                username,
                password);
        System.out.println(result);
    }

    private void executePostlogin(String command)
        throws Exception {
        switch (command) {
            case "help" -> printPostloginHelp();
            case "logout" -> logout();
            case "create" -> createGame();
            case "list" -> listGames();
            case "join" -> playGame();
            case "observe" -> observeGame();
            default ->
                System.out.println(
                        "Unknown command. Type 'help'.");
        }
    }

    private void printPostloginHelp() {
        System.out.println("""
                create - a game
                list - games
                join - a game
                observe - a game
                logout - when you are done
                quit - playing chess
                help - with possible commands
                """);
    }

    private void logout() throws Exception {
        System.out.println(client.logout());
    }

    private void executeGameplay(
            String command)
        throws Exception {
        switch (command) {
            case "help" ->
                printGameplayHelp();
            case "redraw" ->
                redrawBoard();
            case "move" ->
                makeMove();
            case "highlight" ->
                highlightMoves();
            case "resign" ->
                resignGame();
            case "leave" ->
                leaveGame();
            default ->
                System.out.println(
                        "Unknown command.");
        }
    }

    private void printGameplayHelp() {
        System.out.println("""
                redraw - redraw chess board
                move - make a move
                highlight - highlight legal moves
                leave - leave game
                resign - resign game
                help - display commands
                """);
    }

    private void leaveGame() throws Exception {
        client.leaveGame();
        System.out.println(
                "Left game.");
    }

    private void resignGame() throws Exception {
        System.out.print(
                "Are you sure? (yes/no); ");
        String answer = scanner.nextLine();
        if (answer.equalsIgnoreCase("yes")) {
            client.resignGame();
            System.out.println(
                    "You resigned.");
        }
    }

    private void redrawBoard() {
        ChessGame game =
                client.getCurrentGame();
        if (game == null) {
            return;
        }
        ChessGame.TeamColor perspective =
                ChessGame.TeamColor.WHITE;
        if ("BLACK".equals(
                client.getPlayerColor())) {
            perspective =
                    ChessGame.TeamColor.BLACK;
        }
        BoardRenderer.drawBoard(
                game.getBoard(),
                perspective);
    }

    private void highlightMoves() {
        try {
            System.out.print("Piece position: ");
            String square = scanner.nextLine();
            ChessPosition position = parsePosition(square);
            ChessGame game = client.getCurrentGame();
            var moves = game.validMoves(position);
            if (moves == null || moves.isEmpty()) {
                System.out.println(
                        "No legal moves.");
                return;
            }
            BoardRenderer.drawHighlightedBoard(
                    game.getBoard(),
                    position,
                    moves,
                    determinePerspective());
        } catch (Exception ex) {
            System.out.println(
                    ex.getMessage());
        }
    }

    private ChessGame.TeamColor determinePerspective() {
        if ("BLACK".equals(
                client.getPlayerColor())) {
            return ChessGame.TeamColor.BLACK;
        }
        return ChessGame.TeamColor.WHITE;
    }



    private void makeMove() {
        try {
            System.out.print(
                    "From (e.g. e2); ");
            String start =
                    scanner.nextLine();
            System.out.print("To (e.g. e4): ");
            String end = scanner.nextLine();
            ChessPosition startPos =
                    parsePosition(start);
            ChessPosition endPos =
                    parsePosition(end);
            ChessMove move =
                    new ChessMove(
                            startPos,
                            endPos,
                            null);
            client.makeMove(move);
        } catch (Exception ex) {
            System.out.println(
                    ex.getMessage());
        }
    }

    private ChessPosition parsePosition(String text) {
        text = text.trim().toLowerCase();
        if (text.length() != 2) {
            throw new IllegalArgumentException(
                    "Invalid square");
        }
        char file = text.charAt(0);
        char rank = text.charAt(1);
        int col = file - 'a' + 1;
        int row = rank - '0';
        return new ChessPosition(row, col);
    }

    private void createGame() throws Exception {
        System.out.print("Game Name: ");
        String gameName = scanner.nextLine();
        System.out.println(
                client.createGame(
                        gameName));
    }

    private void listGames() throws Exception {
        var games = client.listGames();
        if (games.isEmpty()) {
            System.out.println("No games found.");
            return;
        }
        int index = 1;
        for (var game : games) {
            System.out.printf(
                    "%d. %s%n",
                    index++,
                    game.gameName());
            System.out.printf(
                    "   White: %s%n",
                    game.whiteUsername());
            System.out.printf(
                    "   Black: %s%n%n",
                    game.blackUsername());
        }
    }

    private void playGame() {
        try {
            System.out.print("Game Number: ");
            int gameNumber =
                    Integer.parseInt(
                            scanner.nextLine());
            System.out.print(" Color (WHITE/BLACK): ");
            String color = scanner.nextLine();
            System.out.println(
                    client.joinGame(
                            color,
                            gameNumber));
        } catch (NumberFormatException ex) {
            System.out.println(
                    "Please enter a valid game number.");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void observeGame() {
        try {
            System.out.print("Game Number: ");
            int gameNumber =
                    Integer.parseInt(
                            scanner.nextLine());
            client.observeGame(gameNumber);
        } catch (NumberFormatException ex) {
            System.out.println(
                    "Please enter a valid game number.");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
