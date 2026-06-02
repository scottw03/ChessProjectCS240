package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import service.requests.*;
import service.results.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collection;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
    }

    public AuthData register(String username,
                             String password,
                             String email) throws Exception {
        RegisterRequest request =
                new RegisterRequest(username, password, email);
        return makeRequest(
                "POST",
                "/user",
                request,
                null,
                AuthData.class);
    }

    public AuthData login(String username,
                          String password) throws Exception {
        LoginRequest request =
                new LoginRequest(username, password);
        return makeRequest(
                "POST",
                "/session",
                request,
                null,
                AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        makeRequest(
                "DELETE",
                "/session",
                null,
                authToken,
                null);
    }

    public CreateGameResult createGame(String authToken,
                                       String gameName) throws Exception {
        return null;
    }

    public ListGamesResult listGames(String authToken) throws Exception {
        return null;
    }

    public void joinGame(String authToken,
                         String color,
                         int gameID) throws Exception {

    }

    public void clear() throws Exception {

    }

    private <T> T makeRequest(
            String method,
            String path,
            Object request,
            String authToken,
            Class<T> responseClass)
        throws Exception {
        URI uri = new URI(serverUrl + path);
        HttpURLConnection connection =
                (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod(method);
        if (authToken != null) {
            connection.setRequestProperty(
                    "Authorization",
                    authToken);
        }
        connection.setRequestProperty(
                "Content-Type",
                "application/json");
        if (request != null) {
            connection.setDoOutput(true);
            try (OutputStream body =
                    connection.getOutputStream()) {
                String json = gson.toJson(request);
                body.write(json.getBytes());
            }
        }
        connection.connect();
        int statusCode = connection.getResponseCode();
        if (statusCode >= 400) {
            try (InputStream errorStream =
                    connection.getErrorStream()) {
                ErrorResponse error =
                        gson.fromJson(
                                new String(errorStream.readAllBytes()),
                                ErrorResponse.class);
                throw new Exception(error.message());
            }
        }
        try (InputStream responseBody =
                connection.getInputStream()) {
            String json =
                    new String(responseBody.readAllBytes());
            return gson.fromJson(json, responseClass);
        }
    }
}
