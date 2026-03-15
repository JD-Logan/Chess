package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.jetbrains.annotations.NotNull;
import service.ClearService;
import service.GameService;
import service.UserService;
import io.javalin.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final DataAccess dataAccess = new MemoryDataAccess();
    private final ClearService clearService = new ClearService(dataAccess);
    private final UserService userService = new UserService(dataAccess);
    private final GameService gameService = new GameService(dataAccess);
    private final Gson gson = new Gson();

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.get("/", this::serveIndex);
        javalin.delete("/db", this::handleClear);
        javalin.post("/user", this::handleRegister);
        javalin.post("/session", this::handleLogin);
        javalin.delete("/session", this::handleLogout);
        javalin.get("/game", this::handleListGames);
        javalin.post("/game", this::handleCreateGame);
        javalin.put("/game", this::handleJoinGame);

    }

    private void serveIndex(Context context) {
        try (InputStream is = Server.class.getClassLoader().getResourceAsStream("web/index.html")) {
            if (is == null) {
                context.status(500);
                return;
            }
            context.contentType("text/html");
            context.result(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            context.status(500);
        }
    }

    private void handleClear(Context context) {
        try {
            clearService.clear();
            context.status(200);
            context.result("{}");
        } catch (DataAccessException e) {
            context.status(500);
            context.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void handleRegister(Context context) {
        try {
            UserData request = gson.fromJson(context.body(), UserData.class);
            AuthData auth = userService.register(request);
            context.result(gson.toJson(Map.of(
                    "username", auth.username(),
                    "authToken", auth.authToken()
            )));
        } catch (IllegalArgumentException e) {
            context.status(400);
            context.result(gson.toJson(Map.of("message", "Error: bad request")));
        } catch (IllegalStateException e) {
            context.status(403);
            context.result(gson.toJson(Map.of("message", "error: already taken")));
        } catch (DataAccessException e) {
            context.status(500);
            context.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void handleLogin(Context context) {
        try {
            UserData request = gson.fromJson(context.body(), UserData.class);
            AuthData auth = userService.register(request);
            context.result(gson.toJson(Map.of(
                    "username", auth.username(),
                    "authToken", auth.authToken()
            )));
        } catch (IllegalArgumentException e) {
            context.status(400);
        } catch (SecurityException e) {
            context.status(401);
            context.result(gson.toJson(Map.of("message", "Error: unauthorized")));
        } catch(DataAccessException e){
            context.status(500);
            context.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void handleLogout(Context context) {
        try {
            UserData request = gson.fromJson(context.body(), UserData.class);
            AuthData auth = userService.register(request);
            context.result(gson.toJson(Map.of(
                    "username", auth.username(),
                    "authToken", auth.authToken()
            )));
        } catch (IllegalArgumentException e) {
            context.status(400);
            context.result(gson.toJson(Map.of("message", "Error: bad request")));
        } catch(DataAccessException e){
            context.status(500);
            context.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void handleListGames(Context context) {
        try {

        } catch (IllegalArgumentException e) {
            context.status(400);
            context.result(gson.toJson(Map.of("message", "Error: bad request")));
        } catch (DataAccessException e) {
            context.status(500);
            context.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void handleCreateGame(Context context) {
        try {

        } catch (IllegalArgumentException e) {
            context.status(400);
            context.result(gson.toJson(Map.of("message", "Error: bad request")));
        } catch (DataAccessException e) {
            context.status(500);
            context.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void handleJoinGame(Context context) {
        try {

        } catch (IllegalArgumentException e) {
            context.status(400);
            context.result(gson.toJson(Map.of("message", "Error: bad request")));
        } catch (DataAccessException e) {
            context.status(500);
            context.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
