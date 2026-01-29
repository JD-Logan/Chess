package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        throw new RuntimeException("Not implemented");
        // is a piece move AND the move doesn't leave your king in check
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
        // Receives a given move and executes it, provided it is a legal move. If the move is illegal, it throws an InvalidMoveException.
        // A move is illegal if it is not a "valid" move for the piece at the starting location, or if it’s not the corresponding team's turn.
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = scanForKing(teamColor);

        TeamColor opponentsColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        List<ChessMove> opponentsMoves = new ArrayList<>();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece pieceAtPos = board.getPiece(newPos);
                // FINISH THIS> START HERE
            }
        }


        return true;
        // Returns true if the specified team’s King could be captured by an opposing piece
        // isInCheck:
        //- plan:
        //  - scan the board for the king
        //  - get all the piece moves for the opponents pieces
        //  - if any of the moves captures the kings position, return true
    }

    private ChessPosition scanForKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece pieceAtPos = board.getPiece(newPos);

                if (pieceAtPos != null && pieceAtPos.getPieceType() == ChessPiece.PieceType.KING && pieceAtPos.getTeamColor() == teamColor) {
                    return newPos;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
        // Returns true if the given team has no way to protect their king from being captured.
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
        // Returns true if the given team has no legal moves but their king is not in immediate danger.
    }

    // Extra Credit Moves
    //If you would like to fully implement the rules of chess you need to provide support for Castling and En Passant.
    //You do not have to implement these moves, but if you go the extra mile and successfully implement them, you’ll earn 5 extra credit points for each move (10 total) on this assignment.

    // Castling
    //This is a special move where the King and a Rook move simultaneously. The castling move can only be taken when 3 conditions are met:
    //
    //Neither the King nor Rook have moved since the game started
    //There are no pieces between the King and the Rook
    //The King is never in Check. The King does not start in Check, does not cross a square on which it would be in Check, and is not in Check after castling.
    //To Castle, the King moves 2 spaces towards the Rook, and the Rook "jumps" the king moving to the position next to and on the other side of the King.
    // This is represented in a ChessMove as the king moving 2 spaces to the side.

    // En Passant
    //This is a special move taken by a Pawn in response to your opponent double moving a Pawn.
    // If your opponent double moves a pawn so it ends next to yours (skipping the position where your pawn could have captured their pawn),
    // then on your immediately following turn your pawn may capture their pawn as if their pawn had only moved 1 square.
    // This is as if your pawn is capturing their pawn mid motion, or "In Passing."
    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}
