package client;

import java.util.Scanner;

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
                Available Commands:
                
                help
                register
                login
                quit
                
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

    }
}
