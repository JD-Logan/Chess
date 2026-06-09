package client;

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
                        // postlogin stuff. returns false to stay inl oop
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
            default -> System.out.println("Unrecognized command. Type Help for options");
        }
        return true;
    }

    private void printPostLoginHelp() {
        System.out.println("""
                
               logout - sign out of your account
               help - show this menu again
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
}
