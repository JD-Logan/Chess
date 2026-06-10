package client;

import com.google.gson.Gson;
import model.GameData;
import model.UserData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class ServerFacade {
    private final String serverUrl;
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public ServerFacade(String host, int port) {
        this.serverUrl = "http://" + host + ":" + port;
    }


    private record HttpResult(int statusCode, String body) {}

    private HttpResult makeRequest(String method, String path, Object body, String authToken) throws Exception {
        var builder = HttpRequest.newBuilder().uri(URI.create(serverUrl + path));

        if (authToken != null) {
            builder.header("authorization", authToken);
        }

        if (body != null) {
            String json = gson.toJson(body);
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(json));
        } else {
           builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return new HttpResult(response.statusCode(), response.body());
    }

    public void clear() throws Exception {
        HttpResult result = makeRequest("DELETE", "/db", null, null);
        if (result.statusCode() != 200) {
            throw new RuntimeException("clear() failed " + result.statusCode() + result.body());
        }

    }

    public record AuthResult(String username, String authToken) {}

    public AuthResult register(String username, String password, String email) throws Exception {
        var body = new UserData(username, password, email);
        HttpResult result = makeRequest("POST", "/user", body, null);
        if (result.statusCode() == 200) {
            return gson.fromJson(result.body(), AuthResult.class);
        }

        throw new RuntimeException("register() failed " + result.statusCode() + result.body());
    }

    public AuthResult login(String username, String password) throws Exception {
        var body = new UserData(username, password, null);
        HttpResult result = makeRequest("POST", "/session", body, null);
        if (result.statusCode() == 200) {
            return gson.fromJson(result.body(), AuthResult.class);
        }
        throw new RuntimeException("login() failed " + result.statusCode() + result.body());
    }


    public void logout(String authToken) throws Exception {
        HttpResult result = makeRequest("DELETE", "/session", null, authToken);
        if (result.statusCode() != 200) {
            throw new RuntimeException("logout() failed " + result.statusCode() + result.body());
        }
    }

    public record CreateGameRequest(String gameName) {}

    public record CreateGameResult(int gameID) {}

    public CreateGameResult createGame(String authToken, String gameName) throws Exception {
        var body = new CreateGameRequest(gameName);
        HttpResult result = makeRequest("POST", "/game", body, authToken);
        if (result.statusCode() != 200) {
            throw new RuntimeException("createGame() failed " + result.statusCode() + result.body());
        }
        return gson.fromJson(result.body(), CreateGameResult.class);
    }

    public record JoinGameRequest(String playerColor, int gameID) {}

    public void joinGame(String authToken, String playerColor, int gameID) throws Exception {
        var body = new JoinGameRequest(playerColor, gameID);
        HttpResult result = makeRequest("PUT", "/game", body, authToken);
        if (result.statusCode() != 200) {
            throw new RuntimeException("joinGame() failed " + result.statusCode() + result.body());
        }
    }

    public record ListGamesResult(ArrayList<GameData> games) {}

    public ListGamesResult listGames(String authToken) throws Exception {
        HttpResult result = makeRequest("GET", "/game", null, authToken);
        if (result.statusCode() != 200) {
            throw new RuntimeException("listGames() failed " + result.statusCode() + result.body());
        }
        return gson.fromJson(result.body(), ListGamesResult.class);
    }

}
