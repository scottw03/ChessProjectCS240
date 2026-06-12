package client;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.*;
import websocket.messages.*;

import java.net.URI;

@ClientEndpoint
public class WebSocketCommunicator {
    private final Gson gson = new Gson();
    private Session session;
    private final ServerMessageObserver observer;
    private final String wsUrl;

    public WebSocketCommunicator(
            String wsUrl,
            ServerMessageObserver observer)
        throws Exception {

        this.wsUrl = wsUrl;
        this.observer = observer;
    }

    public void openConnection() throws Exception {
        if (session != null && session.isOpen()) {
            return;
        }

        WebSocketContainer container =
                ContainerProvider.getWebSocketContainer();
        container.connectToServer(
                this,
                URI.create(wsUrl));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String json) {
        try {
            ServerMessage base =
                    gson.fromJson(
                            json,
                            ServerMessage.class);
            switch (base.getServerMessageType()) {
                case LOAD_GAME -> {
                    LoadGameMessage message =
                            gson.fromJson(
                                    json,
                                    LoadGameMessage.class);
                    observer.notify(message);
                }
                case NOTIFICATION -> {
                    NotificationMessage message =
                            gson.fromJson(
                                    json,
                                    NotificationMessage.class);
                    observer.notify(message);
                }
                case ERROR -> {
                    ErrorMessage message =
                            gson.fromJson(
                                    json,
                                    ErrorMessage.class);
                    observer.notify(message);
                }
            }
        } catch (Throwable t) {
            System.out.println(
                    "EXCEPTION INSIDE CLIENT OnMessage");
            t.printStackTrace();
        }
    }

    @OnClose
    public void onClose(
            Session session,
            CloseReason reason) {
        System.out.println(
                "Disconnected from game.");
    }

    @OnError
    public void onError(
            Session session,
            Throwable error) {
        System.out.println(
                "CLIENT WEBSOCKET ERROR");
        error.printStackTrace();
    }

    public void connect(
            String authToken,
            int gameID)
        throws Exception {
        ConnectCommand command =
                new ConnectCommand(
                        authToken,
                        gameID);
        sendCommand(command);
    }

    public void makeMove(
            String authToken,
            int gameID,
            ChessMove move)
        throws Exception {
        MakeMoveCommand command =
                new MakeMoveCommand(
                        authToken,
                        gameID,
                        move);
        sendCommand(command);
    }

    public void leave(
            String authToken,
            int gameID)
        throws Exception {
        LeaveCommand command =
                new LeaveCommand(
                        authToken,
                        gameID);
        sendCommand(command);
    }

    public void resign(
            String authToken,
            int gameID)
        throws Exception {
        ResignCommand command =
                new ResignCommand(
                        authToken,
                        gameID);
        sendCommand(command);
    }

    private void sendCommand(
            UserGameCommand command)
        throws Exception {
        if (session == null) {
            throw new IllegalStateException(
                    "WebSocket not connected yet");
        }
        String json = gson.toJson(command);
        session.getBasicRemote().sendText(json);
    }
}
