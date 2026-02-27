package chess;

import java.util.ArrayList;

public class Chess {

    enum Player {
        white, black
    }

    static Board board = new Board();

    /**
     * Plays the next move for whichever player has the turn.
     * 
     * @param move String for next move, e.g. "a2 a3"
     * 
     * @return A ReturnPlay instance that contains the result of the move.
     *         See the section "The Chess class" in the assignment description for
     *         details of
     *         the contents of the returned ReturnPlay instance.
     */
    public static ReturnPlay play(String move) {
        ReturnPlay result = new ReturnPlay();

        if (move == null || move.trim().isEmpty()) {
            result.message = ReturnPlay.Message.ILLEGAL_MOVE;
            result.piecesOnBoard = board.buildPiecesList();
            return result;
        }

        // Normalize: trim, collapse spaces, lowercase for case-insensitive parsing
        String normalized = move.trim().toLowerCase();
        String[] parts = normalized.split("\\s+");

        // Handle resign
        if (parts[0].equals("resign")) {
            result.piecesOnBoard = board.buildPiecesList();
            if (board.currentTurn == Player.white) {
                result.message = ReturnPlay.Message.RESIGN_BLACK_WINS;
            } else {
                result.message = ReturnPlay.Message.RESIGN_WHITE_WINS;
            }
            return result;
        }

        // Need at least 2 parts for a move
        if (parts.length < 2) {
            result.message = ReturnPlay.Message.ILLEGAL_MOVE;
            result.piecesOnBoard = board.buildPiecesList();
            return result;
        }

        // Parse from/to squares
        String fromStr = parts[0];
        String toStr = parts[1];

        if (fromStr.length() != 2 || toStr.length() != 2) {
            result.message = ReturnPlay.Message.ILLEGAL_MOVE;
            result.piecesOnBoard = board.buildPiecesList();
            return result;
        }

        int fromFile = fromStr.charAt(0) - 'a';
        int fromRank = fromStr.charAt(1) - '1';
        int toFile = toStr.charAt(0) - 'a';
        int toRank = toStr.charAt(1) - '1';

        if (!board.inBounds(fromFile, fromRank) || !board.inBounds(toFile, toRank)) {
            result.message = ReturnPlay.Message.ILLEGAL_MOVE;
            result.piecesOnBoard = board.buildPiecesList();
            return result;
        }

        // Check for draw? and promotion piece
        boolean drawRequest = false;
        String promotionPiece = null;

        for (int i = 2; i < parts.length; i++) {
            if (parts[i].equals("draw?")) {
                drawRequest = true;
            } else if (parts[i].length() == 1 && "qrbn".indexOf(parts[i].charAt(0)) >= 0) {
                promotionPiece = parts[i].toUpperCase();
            }
        }

        // Attempt the move
        boolean success = board.tryMove(fromFile, fromRank, toFile, toRank, promotionPiece);

        if (!success) {
            result.message = ReturnPlay.Message.ILLEGAL_MOVE;
            result.piecesOnBoard = board.buildPiecesList();
            return result;
        }

        // Move succeeded – switch turn
        Player opponent;
        if (board.currentTurn == Player.white) {
            opponent = Player.black;
            board.currentTurn = Player.black;
        } else {
            opponent = Player.white;
            board.currentTurn = Player.white;
        }

        // Evaluate post-move state: checkmate > check > draw > null
        boolean opponentInCheck = board.isInCheck(opponent);
        boolean opponentHasLegalMove = board.hasLegalMove(opponent);

        if (opponentInCheck && !opponentHasLegalMove) {
            // Checkmate takes priority over everything
            if (opponent == Player.white) {
                result.message = ReturnPlay.Message.CHECKMATE_BLACK_WINS;
            } else {
                result.message = ReturnPlay.Message.CHECKMATE_WHITE_WINS;
            }
        } else if (opponentInCheck) {
            // Check takes priority over draw
            result.message = ReturnPlay.Message.CHECK;
        } else if (drawRequest) {
            // Draw only if no check/checkmate
            result.message = ReturnPlay.Message.DRAW;
        } else if (!opponentHasLegalMove) {
            // Stalemate
            result.message = ReturnPlay.Message.STALEMATE;
        } else {
            result.message = null;
        }

        result.piecesOnBoard = board.buildPiecesList();
        return result;
    }

    /**
     * This method should reset the game, and start from scratch.
     */
    public static void start() {
        board = new Board();
        board.init();
    }
}
