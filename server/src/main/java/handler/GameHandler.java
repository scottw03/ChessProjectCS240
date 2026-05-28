package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import service.GameService;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.requests.ListGamesRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;

public class GameHandler {
    private final GameService service;
    private final Gson gson = new Gson();
    public GameHandler(GameService service) {
        this.service = service;
    }
    public void createGame(Context ctx) throws Exception {
        String authToken = ctx.header("authorization");
        CreateGameRequest body = gson.fromJson(ctx.body(), CreateGameRequest.class);
        if (body == null) {
            throw new Exception("bad request");
        }
        CreateGameRequest request = new CreateGameRequest(body.gameName(), authToken);
        CreateGameResult result = service.createGame(request);
        ctx.status(200);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(result));
    }
    public void listGames(Context ctx) throws Exception {
        String authToken = ctx.header("authorization");
        ListGamesRequest request = new ListGamesRequest(authToken);
        ListGamesResult result = service.listGames(request);
        ctx.status(200);
        ctx.result(gson.toJson(result));
    }
    public void joinGame(Context ctx) throws Exception {
        String authToken = ctx.header("authorization");
        JoinGameRequest body =
                gson.fromJson(
                        ctx.body(),
                        JoinGameRequest.class
                );
        if (body == null) {
            throw new Exception("bad request");
        }
        JoinGameRequest request =
                new JoinGameRequest(
                        body.playerColor(),
                        body.gameID(),
                        authToken
                );
        service.joinGame(request);
        ctx.status(200);
        ctx.result("{}");
        ctx.contentType("application/json");
    }
}