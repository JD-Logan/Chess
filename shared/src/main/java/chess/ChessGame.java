package chess;

import java.util.ArrayList;
import java.util.Collection;
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

        ChessPiece pieceAtPos = board.getPiece(startPosition);
        if (pieceAtPos == null) {
            return null;
        }

        Collection<ChessMove> validMoves = new ArrayList<>();
        Collection<ChessMove> pieceAtPosMoves = pieceAtPos.pieceMoves(board, startPosition);
        TeamColor pieceColor = pieceAtPos.getTeamColor();

        for (ChessMove move : pieceAtPosMoves) {
            ChessBoard copiedBoard = cloneBoard(this.board);

            ChessPiece playPiece = copiedBoard.getPiece(move.getStartPosition());
            copiedBoard.addPiece(move.getStartPosition(), null);
            copiedBoard.addPiece(move.getEndPosition(), playPiece);

            if (!isInCheck(pieceColor, copiedBoard)) {
                // I need to change the parameters of isInCheck. is that legal with the autograder?
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    private ChessBoard cloneBoard(ChessBoard board) {
        ChessBoard copyBoard = new ChessBoard();
        for (int row = 1; row <= 8; row++)
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece pieceAtPos = board.getPiece(pos);

                if (pieceAtPos != null) {
                    ChessPiece copyPiece = new ChessPiece(pieceAtPos.getTeamColor(), pieceAtPos.getPieceType());
                    copyBoard.addPiece(pos, copyPiece);
                }
            }
        return copyBoard;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException("There is no piece there");
        }

        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Not your turn!!!");
        }

        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("That's an invalid move");
        }

        board.addPiece(move.getStartPosition(), null);

        if (move.getPromotionPiece() != null) {
            ChessPiece pawnPromotion = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            board.addPiece(move.getEndPosition(), pawnPromotion);
        } else {
            board.addPiece(move.getEndPosition(), piece);
        }

        teamTurn = (this.teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */

    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, this.board);
    }

    private boolean isInCheck(TeamColor teamColor, ChessBoard checkingBoard) {
        ChessPosition kingPosition = scanForKing(teamColor, checkingBoard);

        TeamColor opponentsColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        for (int row = 1; row <= 8; row++)
            for (int col = 1; col <= 8; col++) {
                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece pieceAtPos = checkingBoard.getPiece(newPos);

                if (pieceAtPos != null && pieceAtPos.getTeamColor() == opponentsColor) {
                    Collection<ChessMove> opponentsMoves = pieceAtPos.pieceMoves(checkingBoard, newPos);

                    for (ChessMove move : opponentsMoves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        return false;
    }

    private ChessPosition scanForKing(TeamColor teamColor, ChessBoard scanBoard) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece pieceAtPos = scanBoard.getPiece(newPos);

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
        if (!isInCheck(teamColor)) {
            return false;
        } else {
            return noMovesPossible(teamColor);
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        } else {
            return noMovesPossible(teamColor);
        }
    }

    private boolean noMovesPossible(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece pieceAtPos = this.board.getPiece(newPos);

                if (pieceAtPos != null && pieceAtPos.getTeamColor() == teamColor) {
                    Collection<ChessMove> validMoves = validMoves(newPos);

                    if (validMoves != null && !validMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

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
