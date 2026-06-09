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
        LOGGEN_IN
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
                    if () {
                        return; // prelogin stuff
                    }
                } else {
                    if () {
                        // postlogin stuff
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

    }

    private void register() {

    }

    private void login() {

    }
}
