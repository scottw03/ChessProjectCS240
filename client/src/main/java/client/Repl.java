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

    }





    private void executePostlogin(String command)
        throws Exception {

    }
}
