package client;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.Scanner;

public class ChessClient {
    private final ServerFacade server;
    private final Scanner scanner = new Scanner(System.in);
    private State state = State.LOGGED_OUT;
    private String username;
    private String authToken;
    private enum State {
        LOGGED_OUT,
        LOGGED_IN
    }

    public ChessClient(ServerFacade server) {
        this.server = server;
    }

    public void run() {
        System.out.println("Welcome to 240 chess. Type Help to get started.");
        while (true) {
            System.out.print(state == State.LOGGED_OUT ? "[LOGGED_OUT] >>> " : "[LOGGED_IN] >>> ");
            String input = scanner.nextLine().trim();

            try {
                if (state == State.LOGGED_OUT) {
                    if (!handlePreLogin(input)) {
                        return; // prelogin stuff.return quits loop.
                    }
                } else {
                    if (!handlePostLogin(input)) {
                        return; // postlogin stuff. returns false to stay inl oop
                    }
                }
            } catch (Exception e) {
                System.out.println("Error " + e);
            }
        }
    }

    private boolean handlePreLogin(String input) {
        switch (input.toLowerCase()) {
            case "help" -> printPreLoginHelp();
            case "quit" -> {
                System.out.println("Bye");
                return false;
            }
            case "register" -> register();
            case "login" -> login();
            default -> System.out.println("unrecognized command. Type Help for options.");
        }
        return true;
    }

    private void printPreLoginHelp() {
        System.out.println("""
                
               register - create your account to sign in
               login - sign in with your account
               quit - leave the chess program
               help - show this menu again
               """);
    }

    private void register() {
        System.out.print("Username: ");
        String user = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        
        if (user.isEmpty() || password.isEmpty() || email.isEmpty()) {
            System.out.println("Username, password and email are required fields.");
            return;
        }
        try {
            ServerFacade.AuthResult auth = server.register(user, password, email);
            username = auth.username();
            authToken = auth.authToken();
            state = State.LOGGED_IN;
            System.out.println("You have registered as " + username);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void login() {
        System.out.print("Username: ");
        String user = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (user.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password are required fields.");
            return;
        }
        try {
            ServerFacade.AuthResult auth = server.login(user, password);
            username = auth.username();
            authToken = auth.authToken();
            state = State.LOGGED_IN;
            System.out.println("You have logged in as " + username);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private boolean handlePostLogin(String input) {
        switch (input.toLowerCase()) {
            case "help" -> printPostLoginHelp();
            case "logout" -> logout();
            case "quit" -> {
                System.out.println("Bye");
                return false;
            }
            case "create game" -> createGame();
            case "list games" -> listGames();
            case "play game" -> playGame();
            case "watch game" -> watchGame();

            default -> System.out.println("Unrecognized command. Type Help for options");
        }
        return true;
    }

    private void printPostLoginHelp() {
        System.out.println("""
                
               logout - sign out of your account
               help - show this menu again
               quit - exit the program
               create game - start a new game
               list games- show all current games
               play game - join a game
               watch game - watch a current game
               """);
    }

    private void logout() {
        try {
            server.logout(authToken);
            authToken = null;
            username = null;
            state = State.LOGGED_OUT;
            System.out.println("You have successfully logged out.");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void createGame() {
        System.out.print("Game name: ");
        String gameName = scanner.nextLine().trim();

        if (gameName.isEmpty()) {
            System.out.println("Game name is a required field");
            return;
        }
        try {
            server.createGame(authToken, gameName);
            System.out.println("Game " + gameName + " created");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private ArrayList<GameData> gameList = new ArrayList<>();

    private void listGames() {
        try {
            ServerFacade.ListGamesResult result = server.listGames(authToken);
            gameList = result.games();
            if (gameList == null || gameList.isEmpty()) {
                System.out.println("There are no current games");
                return;
            }

            for (GameData game : gameList) {
                System.out.printf("%d. %s | White: %s | Black: %s%n",
                        game.gameName(),
                        game.whiteUsername(),
                        game.blackUsername());
            }

        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

    private void playGame() {
        listGames();
        System.out.print("Which game?: ");
        String line = scanner.nextLine().trim();
        int gameNum = 0;
        try {
            gameNum = Integer.parseInt(line);
        } catch (Exception e) {
            System.out.println("Invalid game ID");
        }
        if (gameNum < 1 || gameNum > gameList.size()) {
            System.out.println("Invalid game ID");
            return;
        }

        System.out.print("Join as White or Black: ");
        String color = scanner.nextLine().trim().toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            System.out.println("color ust be either WHITE or BLACK.");
            return;
        }

        GameData game = gameList.get(gameNum -1);
        try {
            server.joinGame(authToken, color, game.gameID());
            ChessGame chessGame = game.game() != null ? game.game() : new ChessGame();
            // func to draw the board
        } catch (Exception e) {
            System.out.println(e);
        }



    }

    private void watchGame() {
        listGames();
        if (gameList.isEmpty()) {
            System.out.println("There are no games to watch");
            return;
        }

        System.out.print("Which game?: ");
        String line = scanner.nextLine().trim();
        int gameNum = 0;
        try {
            gameNum = Integer.parseInt(line);
        } catch (Exception e) {
            System.out.println("Invalid game ID");
        }
        if (gameNum < 1 || gameNum > gameList.size()) {
            System.out.println("Invalid game ID");
            return;
        }

        GameData game = gameList.get(gameNum -1);
        ChessGame chessGame = game.game() != null ? game.game() : new ChessGame();
        // func to draw the board

    }
}
