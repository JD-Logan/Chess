package client;

import chess.*;

public class ClientMain {
    public static void main(String[] args) {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);

        var serverFacade = new ServerFacade("localhost", 8080);
        var client = new ChessClient(serverFacade);
        client.run();
    }
}