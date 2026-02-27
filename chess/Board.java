package chess;

import java.util.ArrayList;

/**
 * Internal board representation and move logic helper.
 */
class Board {

    // board[file 0..7][rank 0..7] stores piece type or null
    // file 0 = a, rank 0 = rank 1
    ReturnPiece.PieceType[][] squares = new ReturnPiece.PieceType[8][8];

    boolean whiteKingMoved = false;
    boolean blackKingMoved = false;
    boolean whiteRookAMoved = false; // a1 rook
    boolean whiteRookHMoved = false; // h1 rook
    boolean blackRookAMoved = false; // a8 rook
    boolean blackRookHMoved = false; // h8 rook

    // en passant target: file index (0-7) where en passant capture is possible, -1
    // if none
    int enPassantFile = -1;
    // the rank of the pawn that can be captured en passant (the pawn itself, not
    // the target square)
    int enPassantRank = -1;

    Chess.Player currentTurn = Chess.Player.white;

    void init() {
        // clear
        for (int f = 0; f < 8; f++)
            for (int r = 0; r < 8; r++)
                squares[f][r] = null;

        // White pieces (rank 1 = index 0)
        squares[0][0] = ReturnPiece.PieceType.WR;
        squares[1][0] = ReturnPiece.PieceType.WN;
        squares[2][0] = ReturnPiece.PieceType.WB;
        squares[3][0] = ReturnPiece.PieceType.WQ;
        squares[4][0] = ReturnPiece.PieceType.WK;
        squares[5][0] = ReturnPiece.PieceType.WB;
        squares[6][0] = ReturnPiece.PieceType.WN;
        squares[7][0] = ReturnPiece.PieceType.WR;
        for (int f = 0; f < 8; f++)
            squares[f][1] = ReturnPiece.PieceType.WP;

        // Black pieces (rank 8 = index 7)
        squares[0][7] = ReturnPiece.PieceType.BR;
        squares[1][7] = ReturnPiece.PieceType.BN;
        squares[2][7] = ReturnPiece.PieceType.BB;
        squares[3][7] = ReturnPiece.PieceType.BQ;
        squares[4][7] = ReturnPiece.PieceType.BK;
        squares[5][7] = ReturnPiece.PieceType.BB;
        squares[6][7] = ReturnPiece.PieceType.BN;
        squares[7][7] = ReturnPiece.PieceType.BR;
        for (int f = 0; f < 8; f++)
            squares[f][6] = ReturnPiece.PieceType.BP;

        whiteKingMoved = false;
        blackKingMoved = false;
        whiteRookAMoved = false;
        whiteRookHMoved = false;
        blackRookAMoved = false;
        blackRookHMoved = false;
        enPassantFile = -1;
        enPassantRank = -1;
        currentTurn = Chess.Player.white;
    }

    Board copy() {
        Board b = new Board();
        for (int f = 0; f < 8; f++)
            for (int r = 0; r < 8; r++)
                b.squares[f][r] = this.squares[f][r];
        b.whiteKingMoved = this.whiteKingMoved;
        b.blackKingMoved = this.blackKingMoved;
        b.whiteRookAMoved = this.whiteRookAMoved;
        b.whiteRookHMoved = this.whiteRookHMoved;
        b.blackRookAMoved = this.blackRookAMoved;
        b.blackRookHMoved = this.blackRookHMoved;
        b.enPassantFile = this.enPassantFile;
        b.enPassantRank = this.enPassantRank;
        b.currentTurn = this.currentTurn;
        return b;
    }

    boolean isWhite(ReturnPiece.PieceType pt) {
        if (pt == null)
            return false;
        return pt.name().charAt(0) == 'W';
    }

    boolean isBlack(ReturnPiece.PieceType pt) {
        if (pt == null)
            return false;
        return pt.name().charAt(0) == 'B';
    }

    boolean isOwnPiece(int file, int rank, Chess.Player player) {
        ReturnPiece.PieceType pt = squares[file][rank];
        if (pt == null)
            return false;
        if (player == Chess.Player.white)
            return isWhite(pt);
        else
            return isBlack(pt);
    }

    boolean isEnemyPiece(int file, int rank, Chess.Player player) {
        ReturnPiece.PieceType pt = squares[file][rank];
        if (pt == null)
            return false;
        if (player == Chess.Player.white)
            return isBlack(pt);
        else
            return isWhite(pt);
    }

    boolean inBounds(int file, int rank) {
        return file >= 0 && file < 8 && rank >= 0 && rank < 8;
    }

    /**
     * Check if a square is attacked by 'attacker' player.
     */
    boolean isSquareAttacked(int file, int rank, Chess.Player attacker) {
        // Check pawn attacks
        if (attacker == Chess.Player.white) {
            // white pawns attack diagonally upward (rank+1)
            if (inBounds(file - 1, rank - 1) && squares[file - 1][rank - 1] == ReturnPiece.PieceType.WP)
                return true;
            if (inBounds(file + 1, rank - 1) && squares[file + 1][rank - 1] == ReturnPiece.PieceType.WP)
                return true;
        } else {
            // black pawns attack diagonally downward (rank-1)
            if (inBounds(file - 1, rank + 1) && squares[file - 1][rank + 1] == ReturnPiece.PieceType.BP)
                return true;
            if (inBounds(file + 1, rank + 1) && squares[file + 1][rank + 1] == ReturnPiece.PieceType.BP)
                return true;
        }

        // Check knight attacks
        ReturnPiece.PieceType attackerKnight = (attacker == Chess.Player.white) ? ReturnPiece.PieceType.WN
                : ReturnPiece.PieceType.BN;
        int[][] knightMoves = { { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 }, { 1, 2 }, { 2, -1 },
                { 2, 1 } };
        for (int[] km : knightMoves) {
            int nf = file + km[0], nr = rank + km[1];
            if (inBounds(nf, nr) && squares[nf][nr] == attackerKnight)
                return true;
        }

        // Check king attacks
        ReturnPiece.PieceType attackerKing = (attacker == Chess.Player.white) ? ReturnPiece.PieceType.WK
                : ReturnPiece.PieceType.BK;
        for (int df = -1; df <= 1; df++) {
            for (int dr = -1; dr <= 1; dr++) {
                if (df == 0 && dr == 0)
                    continue;
                int nf = file + df, nr = rank + dr;
                if (inBounds(nf, nr) && squares[nf][nr] == attackerKing)
                    return true;
            }
        }

        // Check rook/queen attacks (straight lines)
        ReturnPiece.PieceType attackerRook = (attacker == Chess.Player.white) ? ReturnPiece.PieceType.WR
                : ReturnPiece.PieceType.BR;
        ReturnPiece.PieceType attackerQueen = (attacker == Chess.Player.white) ? ReturnPiece.PieceType.WQ
                : ReturnPiece.PieceType.BQ;
        int[][] straightDirs = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
        for (int[] d : straightDirs) {
            int nf = file + d[0], nr = rank + d[1];
            while (inBounds(nf, nr)) {
                ReturnPiece.PieceType p = squares[nf][nr];
                if (p != null) {
                    if (p == attackerRook || p == attackerQueen)
                        return true;
                    break;
                }
                nf += d[0];
                nr += d[1];
            }
        }

        // Check bishop/queen attacks (diagonals)
        ReturnPiece.PieceType attackerBishop = (attacker == Chess.Player.white) ? ReturnPiece.PieceType.WB
                : ReturnPiece.PieceType.BB;
        int[][] diagDirs = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
        for (int[] d : diagDirs) {
            int nf = file + d[0], nr = rank + d[1];
            while (inBounds(nf, nr)) {
                ReturnPiece.PieceType p = squares[nf][nr];
                if (p != null) {
                    if (p == attackerBishop || p == attackerQueen)
                        return true;
                    break;
                }
                nf += d[0];
                nr += d[1];
            }
        }

        return false;
    }

    /**
     * Find the king's position for the given player.
     */
    int[] findKing(Chess.Player player) {
        ReturnPiece.PieceType kingType = (player == Chess.Player.white) ? ReturnPiece.PieceType.WK
                : ReturnPiece.PieceType.BK;
        for (int f = 0; f < 8; f++)
            for (int r = 0; r < 8; r++)
                if (squares[f][r] == kingType)
                    return new int[] { f, r };
        return null; // should never happen
    }

    boolean isInCheck(Chess.Player player) {
        int[] king = findKing(player);
        Chess.Player opponent = (player == Chess.Player.white) ? Chess.Player.black : Chess.Player.white;
        return isSquareAttacked(king[0], king[1], opponent);
    }

    /**
     * Check if a pseudo-legal move is valid (doesn't leave own king in check).
     * Simulates the move on a copy and checks.
     */
    boolean doesMoveLeaveKingSafe(int fromFile, int fromRank, int toFile, int toRank, Chess.Player player) {
        Board sim = this.copy();
        // handle en passant capture in simulation
        ReturnPiece.PieceType movingPiece = sim.squares[fromFile][fromRank];
        if ((movingPiece == ReturnPiece.PieceType.WP || movingPiece == ReturnPiece.PieceType.BP)
                && fromFile != toFile && sim.squares[toFile][toRank] == null) {
            // en passant capture
            sim.squares[toFile][fromRank] = null;
        }
        sim.squares[toFile][toRank] = sim.squares[fromFile][fromRank];
        sim.squares[fromFile][fromRank] = null;
        return !sim.isInCheck(player);
    }

    /**
     * Validate and execute a move. Returns true if successful.
     * Updates board state including castling flags, en passant, etc.
     * promotionPiece: null if not promoting, or the letter Q/R/B/N.
     */
    boolean tryMove(int fromFile, int fromRank, int toFile, int toRank, String promotionPiece) {
        ReturnPiece.PieceType piece = squares[fromFile][fromRank];
        if (piece == null)
            return false;

        // Must be own piece
        if (!isOwnPiece(fromFile, fromRank, currentTurn))
            return false;

        // Cannot capture own piece
        if (isOwnPiece(toFile, toRank, currentTurn))
            return false;

        // Validate piece-specific movement
        if (!isValidPieceMove(fromFile, fromRank, toFile, toRank, piece))
            return false;

        // Check that move doesn't leave own king in check
        // Special handling for castling (checked separately in isValidPieceMove)
        boolean isCastling = isCastlingMove(fromFile, fromRank, toFile, toRank, piece);
        if (!isCastling) {
            if (!doesMoveLeaveKingSafe(fromFile, fromRank, toFile, toRank, currentTurn))
                return false;
        }

        // Execute the move
        executeMove(fromFile, fromRank, toFile, toRank, promotionPiece);
        return true;
    }

    boolean isCastlingMove(int fromFile, int fromRank, int toFile, int toRank, ReturnPiece.PieceType piece) {
        if (piece == ReturnPiece.PieceType.WK && fromFile == 4 && fromRank == 0) {
            if ((toFile == 6 && toRank == 0) || (toFile == 2 && toRank == 0))
                return true;
        }
        if (piece == ReturnPiece.PieceType.BK && fromFile == 4 && fromRank == 7) {
            if ((toFile == 6 && toRank == 7) || (toFile == 2 && toRank == 7))
                return true;
        }
        return false;
    }

    boolean isValidPieceMove(int fromFile, int fromRank, int toFile, int toRank, ReturnPiece.PieceType piece) {
        String name = piece.name();
        char type = name.charAt(1);

        switch (type) {
            case 'P':
                return isValidPawnMove(fromFile, fromRank, toFile, toRank, piece);
            case 'R':
                return isValidRookMove(fromFile, fromRank, toFile, toRank);
            case 'N':
                return isValidKnightMove(fromFile, fromRank, toFile, toRank);
            case 'B':
                return isValidBishopMove(fromFile, fromRank, toFile, toRank);
            case 'Q':
                return isValidQueenMove(fromFile, fromRank, toFile, toRank);
            case 'K':
                return isValidKingMove(fromFile, fromRank, toFile, toRank, piece);
            default:
                return false;
        }
    }

    boolean isValidPawnMove(int fromFile, int fromRank, int toFile, int toRank, ReturnPiece.PieceType piece) {
        boolean isWhitePawn = (piece == ReturnPiece.PieceType.WP);
        int direction = isWhitePawn ? 1 : -1;
        int startRank = isWhitePawn ? 1 : 6;

        int fileDiff = toFile - fromFile;
        int rankDiff = toRank - fromRank;

        // Forward one
        if (fileDiff == 0 && rankDiff == direction && squares[toFile][toRank] == null) {
            return true;
        }

        // Forward two from start
        if (fileDiff == 0 && rankDiff == 2 * direction && fromRank == startRank) {
            // Both intermediate and destination must be empty
            if (squares[fromFile][fromRank + direction] == null && squares[toFile][toRank] == null) {
                return true;
            }
        }

        // Diagonal capture
        if (Math.abs(fileDiff) == 1 && rankDiff == direction) {
            // Normal capture
            if (squares[toFile][toRank] != null && isEnemyPiece(toFile, toRank, currentTurn)) {
                return true;
            }
            // En passant capture
            if (squares[toFile][toRank] == null && toFile == enPassantFile) {
                // The en passant pawn must be on the same rank as the capturing pawn
                if (fromRank == enPassantRank) {
                    return true;
                }
            }
        }

        return false;
    }

    boolean isValidRookMove(int fromFile, int fromRank, int toFile, int toRank) {
        if (fromFile != toFile && fromRank != toRank)
            return false;
        return isPathClear(fromFile, fromRank, toFile, toRank);
    }

    boolean isValidKnightMove(int fromFile, int fromRank, int toFile, int toRank) {
        int fd = Math.abs(toFile - fromFile);
        int rd = Math.abs(toRank - fromRank);
        return (fd == 1 && rd == 2) || (fd == 2 && rd == 1);
    }

    boolean isValidBishopMove(int fromFile, int fromRank, int toFile, int toRank) {
        if (Math.abs(toFile - fromFile) != Math.abs(toRank - fromRank))
            return false;
        if (toFile == fromFile)
            return false;
        return isPathClear(fromFile, fromRank, toFile, toRank);
    }

    boolean isValidQueenMove(int fromFile, int fromRank, int toFile, int toRank) {
        return isValidRookMove(fromFile, fromRank, toFile, toRank) ||
                isValidBishopMove(fromFile, fromRank, toFile, toRank);
    }

    boolean isValidKingMove(int fromFile, int fromRank, int toFile, int toRank, ReturnPiece.PieceType piece) {
        int fd = Math.abs(toFile - fromFile);
        int rd = Math.abs(toRank - fromRank);

        // Normal king move
        if (fd <= 1 && rd <= 1 && (fd + rd > 0)) {
            return true;
        }

        // Castling
        if (fd == 2 && rd == 0) {
            return isValidCastling(fromFile, fromRank, toFile, toRank, piece);
        }

        return false;
    }

    boolean isValidCastling(int fromFile, int fromRank, int toFile, int toRank, ReturnPiece.PieceType piece) {
        Chess.Player player = isWhite(piece) ? Chess.Player.white : Chess.Player.black;
        Chess.Player opponent = (player == Chess.Player.white) ? Chess.Player.black : Chess.Player.white;

        // King must not have moved
        if (player == Chess.Player.white && whiteKingMoved)
            return false;
        if (player == Chess.Player.black && blackKingMoved)
            return false;

        // King must not be in check
        if (isInCheck(player))
            return false;

        boolean kingSide = (toFile > fromFile);

        if (player == Chess.Player.white) {
            if (kingSide) {
                if (whiteRookHMoved)
                    return false;
                if (squares[7][0] != ReturnPiece.PieceType.WR)
                    return false;
                // Path must be clear between king and rook
                if (squares[5][0] != null || squares[6][0] != null)
                    return false;
                // Squares king passes through must not be attacked
                if (isSquareAttacked(5, 0, opponent) || isSquareAttacked(6, 0, opponent))
                    return false;
            } else {
                if (whiteRookAMoved)
                    return false;
                if (squares[0][0] != ReturnPiece.PieceType.WR)
                    return false;
                if (squares[1][0] != null || squares[2][0] != null || squares[3][0] != null)
                    return false;
                if (isSquareAttacked(3, 0, opponent) || isSquareAttacked(2, 0, opponent))
                    return false;
            }
        } else {
            if (kingSide) {
                if (blackRookHMoved)
                    return false;
                if (squares[7][7] != ReturnPiece.PieceType.BR)
                    return false;
                if (squares[5][7] != null || squares[6][7] != null)
                    return false;
                if (isSquareAttacked(5, 7, opponent) || isSquareAttacked(6, 7, opponent))
                    return false;
            } else {
                if (blackRookAMoved)
                    return false;
                if (squares[0][7] != ReturnPiece.PieceType.BR)
                    return false;
                if (squares[1][7] != null || squares[2][7] != null || squares[3][7] != null)
                    return false;
                if (isSquareAttacked(3, 7, opponent) || isSquareAttacked(2, 7, opponent))
                    return false;
            }
        }

        return true;
    }

    boolean isPathClear(int fromFile, int fromRank, int toFile, int toRank) {
        int df = Integer.signum(toFile - fromFile);
        int dr = Integer.signum(toRank - fromRank);
        int f = fromFile + df, r = fromRank + dr;
        while (f != toFile || r != toRank) {
            if (squares[f][r] != null)
                return false;
            f += df;
            r += dr;
        }
        return true;
    }

    void executeMove(int fromFile, int fromRank, int toFile, int toRank, String promotionPiece) {
        ReturnPiece.PieceType piece = squares[fromFile][fromRank];
        boolean isCastling = isCastlingMove(fromFile, fromRank, toFile, toRank, piece);

        // Handle en passant capture
        if ((piece == ReturnPiece.PieceType.WP || piece == ReturnPiece.PieceType.BP)
                && fromFile != toFile && squares[toFile][toRank] == null) {
            squares[toFile][fromRank] = null; // remove captured pawn
        }

        // Move piece
        squares[toFile][toRank] = piece;
        squares[fromFile][fromRank] = null;

        // Handle castling: also move the rook
        if (isCastling) {
            boolean kingSide = (toFile > fromFile);
            int rookFromFile, rookToFile;
            int rank = fromRank;
            if (kingSide) {
                rookFromFile = 7;
                rookToFile = 5;
            } else {
                rookFromFile = 0;
                rookToFile = 3;
            }
            squares[rookToFile][rank] = squares[rookFromFile][rank];
            squares[rookFromFile][rank] = null;
        }

        // Handle promotion
        if (piece == ReturnPiece.PieceType.WP && toRank == 7) {
            squares[toFile][toRank] = getPromotionPiece(promotionPiece, Chess.Player.white);
        } else if (piece == ReturnPiece.PieceType.BP && toRank == 0) {
            squares[toFile][toRank] = getPromotionPiece(promotionPiece, Chess.Player.black);
        }

        // Update en passant state
        enPassantFile = -1;
        enPassantRank = -1;
        if (piece == ReturnPiece.PieceType.WP && fromRank == 1 && toRank == 3) {
            enPassantFile = fromFile;
            enPassantRank = 3; // the rank of the pawn that moved
        } else if (piece == ReturnPiece.PieceType.BP && fromRank == 6 && toRank == 4) {
            enPassantFile = fromFile;
            enPassantRank = 4;
        }

        // Update castling flags
        if (piece == ReturnPiece.PieceType.WK)
            whiteKingMoved = true;
        if (piece == ReturnPiece.PieceType.BK)
            blackKingMoved = true;
        if (piece == ReturnPiece.PieceType.WR) {
            if (fromFile == 0 && fromRank == 0)
                whiteRookAMoved = true;
            if (fromFile == 7 && fromRank == 0)
                whiteRookHMoved = true;
        }
        if (piece == ReturnPiece.PieceType.BR) {
            if (fromFile == 0 && fromRank == 7)
                blackRookAMoved = true;
            if (fromFile == 7 && fromRank == 7)
                blackRookHMoved = true;
        }
        // Also mark rook as moved if it was captured on its starting square
        if (toFile == 0 && toRank == 0)
            whiteRookAMoved = true;
        if (toFile == 7 && toRank == 0)
            whiteRookHMoved = true;
        if (toFile == 0 && toRank == 7)
            blackRookAMoved = true;
        if (toFile == 7 && toRank == 7)
            blackRookHMoved = true;
    }

    ReturnPiece.PieceType getPromotionPiece(String promo, Chess.Player player) {
        if (promo == null || promo.isEmpty())
            promo = "Q";
        if (player == Chess.Player.white) {
            switch (promo) {
                case "R":
                    return ReturnPiece.PieceType.WR;
                case "N":
                    return ReturnPiece.PieceType.WN;
                case "B":
                    return ReturnPiece.PieceType.WB;
                case "Q":
                    return ReturnPiece.PieceType.WQ;
                default:
                    return ReturnPiece.PieceType.WQ;
            }
        } else {
            switch (promo) {
                case "R":
                    return ReturnPiece.PieceType.BR;
                case "N":
                    return ReturnPiece.PieceType.BN;
                case "B":
                    return ReturnPiece.PieceType.BB;
                case "Q":
                    return ReturnPiece.PieceType.BQ;
                default:
                    return ReturnPiece.PieceType.BQ;
            }
        }
    }

    /**
     * Check if the given player has any legal move.
     */
    boolean hasLegalMove(Chess.Player player) {
        for (int ff = 0; ff < 8; ff++) {
            for (int fr = 0; fr < 8; fr++) {
                if (!isOwnPiece(ff, fr, player))
                    continue;
                ReturnPiece.PieceType piece = squares[ff][fr];
                for (int tf = 0; tf < 8; tf++) {
                    for (int tr = 0; tr < 8; tr++) {
                        if (ff == tf && fr == tr)
                            continue;
                        if (isOwnPiece(tf, tr, player))
                            continue;
                        if (!isValidPieceMove(ff, fr, tf, tr, piece))
                            continue;
                        boolean casting = isCastlingMove(ff, fr, tf, tr, piece);
                        if (!casting && doesMoveLeaveKingSafe(ff, fr, tf, tr, player)) {
                            return true;
                        }
                        if (casting) {
                            // Castling validity already checks king safety
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Build the pieces list for ReturnPlay, scanning ranks 1..8 then files a..h.
     */
    ArrayList<ReturnPiece> buildPiecesList() {
        ArrayList<ReturnPiece> pieces = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                if (squares[f][r] != null) {
                    ReturnPiece rp = new ReturnPiece();
                    rp.pieceType = squares[f][r];
                    rp.pieceFile = ReturnPiece.PieceFile.values()[f];
                    rp.pieceRank = r + 1;
                    pieces.add(rp);
                }
            }
        }
        return pieces;
    }
}
