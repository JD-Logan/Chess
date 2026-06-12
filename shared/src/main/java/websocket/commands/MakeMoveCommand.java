package websocket.commands;

import chess.ChessMove;
import websocket.messages.ServerMessage;

public class MakeMoveCommand extends UserGameCommand {
    private final ChessMove move;

    public MakeMoveCommand(UserGameCommand.CommandType commandType,
                           String authToken,
                           Integer gameID,
                           ChessMove move) {
        super(commandType, authToken, gameID);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }
}
