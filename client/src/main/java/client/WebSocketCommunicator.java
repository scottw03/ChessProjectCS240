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

    public WebSocketCommunicator(
            String serverUrl,
            ServerMessageObserver observer)
        throws Exception {

        this.observer = observer;
        String wsUrl =
                serverUrl.replace("http", "ws")
                + "/ws";
        WebSocketContainer container =
                ContainerProvider.getWebSocketContainer();
        container.connectToServer(
                this,
                URI.create(wsUrl));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println(
                "Connected to websocket");
    }

    @OnMessage
    public void onMessage(String json) {
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
    }

    @OnClose
    public void onClose(
            Session session,
            CloseReason reason) {
        System.out.println(
                "WebSocket closed");
    }

    @OnError
    public void onError(
            Session session,
            Throwable error) {
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
        session.getBasicRemote().sendText(
                gson.toJson(command));
    }
}
