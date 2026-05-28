package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import service.UserService;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.requests.LogoutRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

public class UserHandler {
    private final UserService service;
    private final Gson gson = new Gson();
    public UserHandler(UserService service) {
        this.service = service;
    }
    public void register(Context ctx) throws Exception {
        RegisterRequest request =
                gson.fromJson(ctx.body(), RegisterRequest.class);
        if (request == null) {
            throw new Exception("bad request");
        }
        RegisterResult result = service.register(request);
        ctx.status(200);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(result));
    }
    public void login(Context ctx) throws Exception {
        LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
        if (request == null) {
            throw new Exception("bad request");
        }
        LoginResult result = service.login(request);
        ctx.status(200);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(result));
    }
    public void logout(Context ctx) throws Exception {
        String authToken = ctx.header("authorization");
        LogoutRequest request = new LogoutRequest(authToken);
        service.logout(request);
        ctx.status(200);
        ctx.result("{}");
        ctx.contentType("application/json");
    }
}