package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.MySqlDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class Server {

    private final Javalin javalin;
    private final DataAccess dataAccess;
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;
    private final Gson gson = new Gson();

    public Server() {
        try {
            dataAccess = new MySqlDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

//        dataAccess = new MemoryDataAccess();

        clearService = new ClearService(dataAccess);
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);


        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.get("/", this::serveIndex);
        javalin.delete("/db", this::handleClear);
        javalin.post("/user", this::handleRegister);
        javalin.post("/session", this::handleLogin);
        javalin.delete("/session", this::handleLogout);
        javalin.get("/game", this::handleListGames);
        javalin.post("/game", this::handleCreateGame);
        javalin.put("/game", this::handleJoinGame);

        WebSocketHandler webSocketHandler = new WebSocketHandler(dataAccess, gson);
        javalin.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler::onConnect);
            ws.onMessage(webSocketHandler::onMessage);
            ws.onClose(webSocketHandler::onClose);
        });
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
            context.result(gson.toJson(Map.of("message", "Error: already taken")));
        } catch (DataAccessException e) {
            context.status(500);
            context.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void handleLogin(Context context) {
        try {
            UserData request = gson.fromJson(context.body(), UserData.class);
            AuthData auth = userService.login(request.username(), request.password());
            context.result(gson.toJson(Map.of(
                    "username", auth.username(),
                    "authToken", auth.authToken()
            )));
        } catch (IllegalArgumentException e) {
            context.status(400);
            context.result(gson.toJson(Map.of("message", "Error: bad request")));
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
            String authToken = context.header("authorization");
            userService.logout(authToken);
            context.status(200);
            context.result("{}");
        } catch (SecurityException e) {
            context.status(401);
            context.result(gson.toJson(Map.of("message", "Error: unauthorized")));
        } catch(DataAccessException e){
            context.status(500);
            context.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void handleListGames(Context context) {
        try {
            String authToken = context.header("authorization");
            Collection<GameData> games = gameService.listGames(authToken);
            context.result(gson.toJson(Map.of("games", games)));

        } catch (SecurityException e) {
            context.status(401);
            context.result(gson.toJson(Map.of("message", "Error: unauthorized")));
        } catch (DataAccessException e) {
            context.status(500);
            context.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void handleCreateGame(Context context) {
        try {
            String authToken = context.header("authorization");
            Map<String, Object> request = gson.fromJson(context.body(), Map.class);
            String gameName = (String) request.get("gameName");
            GameData game = gameService.createGame(authToken, gameName);
            context.result(gson.toJson(Map.of("gameID", game.gameID())));

        } catch (IllegalArgumentException e) {
            context.status(400);
            context.result(gson.toJson(Map.of("message", "Error: bad request")));
        } catch (SecurityException e) {
            context.status(401);
            context.result(gson.toJson(Map.of("message", "Error: unauthorized")));
        } catch (DataAccessException e) {
            context.status(500);
            context.result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void handleJoinGame(Context context) {
        try {
            String authToken = context.header("authorization");
            Map<String, Object> request = gson.fromJson(context.body(), Map.class);
            String playerColor = (String) request.get("playerColor");
            Object gameIdObject = request.get("gameID");
            if (gameIdObject == null) {
                throw new IllegalArgumentException("bad request");
            }
            int gameId = ((Number) gameIdObject).intValue();

            gameService.joinGame(authToken, playerColor, gameId);
            context.status(200);
            context.result("{}");
        } catch (IllegalArgumentException e) {
            context.status(400);
            context.result(gson.toJson(Map.of("message", "Error: bad request")));
        } catch (SecurityException e) {
            context.status(401);
            context.result(gson.toJson(Map.of("message", "Error: unauthorized")));
        } catch (IllegalStateException e) {
            context.status(403);
            context.result(gson.toJson(Map.of("message", "Error: already taken")));
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
