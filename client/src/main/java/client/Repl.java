package client;

import java.util.Scanner;
import chess.ChessGame;
import ui.BoardRenderer;

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
        if (client.getState() ==
                ChessClient.State.PRELOGIN) {
            executePrelogin(command);
        }
        else {
            executePostlogin(command);
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
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
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
                create <NAME> - a game
                list - games
                join <ID> [WHITE|BLACK] - a game
                observe <ID> - a game
                logout - when you are done
                quit - playing chess
                help - with possible commands
                """);
    }

    private void logout() throws Exception {
        System.out.println(client.logout());
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

    private void playGame() throws Exception {
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
        System.out.println(
                "Successfully joined game.");
        ChessGame game = new ChessGame();
        game.getBoard().resetBoard();
        if (color.equalsIgnoreCase("BLACK")) {
            BoardRenderer.drawBoard(
                    game.getBoard(),
                    ChessGame.TeamColor.BLACK);
        } else {
            BoardRenderer.drawBoard(
                    game.getBoard(),
                    ChessGame.TeamColor.WHITE);
        }
    }

    private void observeGame() throws Exception {
        System.out.print("Game Number: ");
        int gameNumber =
                Integer.parseInt(
                        scanner.nextLine());
        client.observeGame(gameNumber);
        ChessGame game = new ChessGame();
        game.getBoard().resetBoard();
        BoardRenderer.drawBoard(
                game.getBoard(),
                ChessGame.TeamColor.WHITE);
    }

    private void drawBoard(String color) {
        System.out.println();
        System.out.println(
                "Board would be displayed here.");
        System.out.println();
    }

}
