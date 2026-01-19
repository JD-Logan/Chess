package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;
    
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
//        ChessPiece piece = board.getPiece(myPosition);
//        if (piece.getPieceType() == PieceType.BISHOP) {
//            return List.of(new ChessMove(new ChessPosition(5,4), new ChessPosition(1,8), null)); // hardcoded
//        }
//        return List.of();
        // Bishop moves idea
        if (this.type == PieceType.BISHOP) {
            List<ChessMove> moves = new ArrayList<>();
            longRangeMoves(board, myPosition, moves, 1, 1); // UP-RIGHT
            longRangeMoves(board, myPosition, moves, -1, 1); // UP-LEFT
            longRangeMoves(board, myPosition, moves, 1, -1); // DOWN-RIGHT
            longRangeMoves(board, myPosition, moves, -1, -1); // DOWN-LEFT
            return moves;

            // ROOK
        } else if (this.type == PieceType.ROOK) {
            List<ChessMove> moves = new ArrayList<>();
            longRangeMoves(board, myPosition, moves, 1, 0); // UP
            longRangeMoves(board, myPosition, moves, -1, 0); // DOWN
            longRangeMoves(board, myPosition, moves, 0, -1); // LEFT
            longRangeMoves(board, myPosition, moves, 0, 1); // RIGHT

            return moves;

            // QUEEN
        } else if (this.type == PieceType.QUEEN) {
            List<ChessMove> moves = new ArrayList<>();
            longRangeMoves(board, myPosition, moves, 1, 0); // UP
            longRangeMoves(board, myPosition, moves, -1, 0); // DOWN
            longRangeMoves(board, myPosition, moves, 0, -1); // LEFT
            longRangeMoves(board, myPosition, moves, 0, 1); // RIGHT
            longRangeMoves(board, myPosition, moves, 1, 1); // UP-RIGHT
            longRangeMoves(board, myPosition, moves, -1, 1); // UP-LEFT
            longRangeMoves(board, myPosition, moves, 1, -1); // DOWN-RIGHT
            longRangeMoves(board, myPosition, moves, -1, -1); // DOWN-LEFT

            return moves;

            // PAWN
            // NOT COMPLETE
            // NOTE: pawns can only move one direction which is gonna make this weird because white and black need to go their ways. so set a flag for white or black to determine the direction.
            // also probably just do knight and king and do the pawn last.
        } else if (this.type == PieceType.PAWN) {
            List<ChessMove> moves = new ArrayList<>();

            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            ChessPosition newPos = new ChessPosition(row,col);
            ChessPiece pieceAtpos = board.getPiece(newPos);

            while (row <= 8) {
                if (pieceAtpos == null) {
                    moves.add(new ChessMove(myPosition, newPos, null)); // empty spot
                } else if (pieceAtpos.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(myPosition, newPos, null)); // enemy piece
                    break;
                } else {
                    break; // Same team piece
                }
            }

            longRangeMoves(board, myPosition, moves, 1, 0); // UP


            return moves;
        }
        else {
            return List.of();
        }
    }

    private void longRangeMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, int rowChange, int colChange) {
        int row = myPosition.getRow() + rowChange;
        int col = myPosition.getColumn() + colChange;

        while (row >=1 && row <=8 && col >=1 && col <=8) {
            ChessPosition newPos = new ChessPosition(row,col);
            ChessPiece pieceAtpos = board.getPiece(newPos);

            if (pieceAtpos == null) {
                moves.add(new ChessMove(myPosition, newPos, null)); // empty spot
            } else if(pieceAtpos.getTeamColor() != this.pieceColor) {
                moves.add(new ChessMove(myPosition, newPos, null)); // enemy piece
                break;
            } else {
                break; // Same team piece
            }
            row += rowChange;
            col += colChange;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
