package client;

import chess.*;

public class ClientMain {
    public static void main(String[] args) {
        ServerFacade facade = new ServerFacade(8080);
        ChessClient client = new ChessClient(facade);
        Repl repl = new Repl(client);
        repl.run();
    }
}
