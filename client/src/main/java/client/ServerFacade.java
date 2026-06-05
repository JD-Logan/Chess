package client;

import com.google.gson.Gson;
import model.UserData;

import java.net.URI; //
import java.net.http.HttpClient;
import java.net.http.HttpRequest; //
import java.net.http.HttpResponse; //

public class ServerFacade {
    private final String serverUrl; // localhost:8080
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public ServerFacade(String host, int port) {
        this.serverUrl = "http://" + host + ":" + port;
    }

    //
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
        makeRequest("DELETE", "/db", null, null);
    }

    public record AuthResult(String username, String authToken) {}


    //
}
