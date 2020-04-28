/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Square.*;

/** Represents the state of a game of Lines of Action.
 *  @author Hankai Xing
 */
class Board {

    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 60;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        _moves.clear();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Square s = sq(i, j);
                set(s, contents[j][i]);
            }
        }
        _turn = side;
        _moveLimit = DEFAULT_MOVE_LIMIT;
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        _turn = board._turn;
        System.arraycopy(board._board, 0, _board, 0, _board.length);
    }

    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null. */
    void set(Square sq, Piece v, Piece next) {
        if (sq.col() < 0 || sq.col() > 7 || sq.row() < 0 || sq.row() > 7) {
            throw new IllegalArgumentException("Invalid square location");
        }
        _board[sq.index()] = v;
        if (next != null) {
            _turn = next;
        }
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /** Set limit on number of moves by each side that results in a tie to
     *  LIMIT, where 2 * LIMIT > movesMade(). */
    void setMoveLimit(int limit) {
        if (2 * limit <= movesMade()) {
            throw new IllegalArgumentException("move limit too small");
        }
        _moveLimit = 2 * limit;
    }

    /** Assuming isLegal(MOVE), make MOVE. Assumes MOVE.isCapture()
     *  is false. */
    void makeMove(Move move) {
        assert isLegal(move);
        assert !move.isCapture();
        _moves.add(move);
        Square s0 = move.getFrom();
        Piece p = get(s0);
        Square s1 = move.getTo();
        _replaced.add(get(s1));
        set(s0, EMP);
        set(s1, p);
        _turn = _turn.opposite();
        _subsetsInitialized = false;
    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;
        Move m = _moves.remove(_moves.size() - 1);
        Piece replaced = _replaced.remove(_replaced.size() - 1);
        Square s1 = m.getTo();
        Piece p = get(s1);
        Square s0 = m.getFrom();
        set(s1, replaced);
        set(s0, p);
        _turn = _turn.opposite();
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move for the player currently on
     *  move. */
    boolean isLegal(Square from, Square to) {
        Move m = Move.mv(from, to);
        Piece p = get(from);
        return m != null && !blocked(from, to)
                && p == turn() && m.length() == piecesAlongDir(from, to);
    }

    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        return isLegal(move.getFrom(), move.getTo());
    }

    /** Return a sequence of all legal moves from this position. */
    List<Move> legalMoves() {
        List<Move> legals = new ArrayList<>();
        for (Square s : ALL_SQUARES) {
            if (get(s) == turn()) {
                for (Square another : ALL_SQUARES) {
                    Move m = Move.mv(s, another);
                    if (m != null && isLegal(m)) {
                        legals.add(m);
                    }
                }
            }
        }
        return legals;
    }

    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP. */
    Piece winner() {
        computeRegions();
        if (!_winnerKnown) {
            if (piecesContiguous(WP) && !piecesContiguous(BP)) {
                _winner = WP;
                _winnerKnown = true;
            } else if (piecesContiguous(BP) && !piecesContiguous(WP)) {
                _winner = BP;
                _winnerKnown = true;
            } else if (movesMade() >= _moveLimit && !piecesContiguous(BP)
                    && !piecesContiguous(WP)) {
                _winner = EMP;
                _winnerKnown = true;
            } else if (piecesContiguous(BP) && piecesContiguous(WP)) {
                _winner = turn().opposite();
                _winnerKnown = true;
            }
        }
        return _winner;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square. */
    private boolean blocked(Square from, Square to) {
        Piece p0 = get(from);
        Piece p1 = get(to);
        if (p0 == p1) {
            return true;
        }
        int direction = from.direction(to);
        for (int i = 1; i < BOARD_SIZE; i++) {
            if (from.moveDest(direction, i) != null) {
                Square s = from.moveDest(direction, i);
                if (s.equals(to)) {
                    break;
                }
                Piece p = get(s);
                if (p == p0.opposite()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted. */
    private int numContig(Square sq, boolean[][] visited, Piece p) {
        if (p == EMP) {
            return 0;
        }
        if (get(sq) != p) {
            return 0;
        }
        if (visited[sq.row()][sq.col()]) {
            return 0;
        }
        visited[sq.row()][sq.col()] = true;
        int count = 1;
        ArrayList<Square> squares = new ArrayList<>();
        for (int i = 0; i <= 7; i++) {
            Square s = sq.moveDest(i, 1);
            if (s != null) {
                squares.add(s);
            }
        }
        for (Square s : squares) {
            count += numContig(s, visited, p);
        }
        return count;
    }

    /** Set the values of _whiteRegionSizes and _blackRegionSizes. */
    private void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Square s = sq(i, j);
                Piece p = get(s);
                if (p == EMP) {
                    continue;
                }
                if (!visited[s.row()][s.col()]) {
                    int count = numContig(s, visited, p);
                    if (p == WP) {
                        _whiteRegionSizes.add(count);
                    } else {
                        _blackRegionSizes.add(count);
                    }
                }
            }
        }
        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;
    }

    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }

    /** Return the number of pieces along the direction from FROM to TO. */
    private int piecesAlongDir(Square from, Square to) {
        int c = from.col();
        int r = from.row();
        int direction = from.direction(to);
        if (direction == 0 || direction == 4) {
            return colCount(c);
        } else if (direction == 2 || direction == 6) {
            return rowCount(r);
        } else if (direction == 1 || direction == 5) {
            return positiveDiaCount(c, r);
        } else if (direction == 3 || direction == 7) {
            return negativeDiaCount(c, r);
        } else {
            return 0;
        }
    }

    /** Return the number of pieces along the NW SE diagonal of
     * the Square with column number C and row number R. */
    private int negativeDiaCount(int c, int r) {
        int result = 0;
        int tmpc = c;
        int tmpr = r;
        for (; c >= 0 && r < BOARD_SIZE; c--, r++) {
            Square s1 = sq(c, r);
            if (_board[s1.index()] != EMP) {
                result++;
            }
        }
        if (tmpc >= 0 && tmpr >= 0) {
            tmpc++;
            tmpr--;
        }
        for (; tmpc < BOARD_SIZE && tmpr >= 0; tmpc++, tmpr--) {
            Square s2 = sq(tmpc, tmpr);
            if (_board[s2.index()] != EMP) {
                result++;
            }
        }
        return result;
    }

    /** Return the number of pieces along the NE SW diagonal of
     * the Square with column number C and row number R. */
    private int positiveDiaCount(int c, int r) {
        int result = 0;
        int tmpc = c;
        int tmpr = r;
        for (; c < BOARD_SIZE && r < BOARD_SIZE; c++, r++) {
            Square s1 = sq(c, r);
            if (_board[s1.index()] != EMP) {
                result++;
            }
        }
        if (tmpc >= 0 && tmpr >= 0) {
            tmpc--;
            tmpr--;
        }
        for (; tmpc >= 0 && tmpr >= 0; tmpc--, tmpr--) {
            Square s2 = sq(tmpc, tmpr);
            if (_board[s2.index()] != EMP) {
                result++;
            }
        }
        return result;
    }

    /** Return the number of pieces along the vertical line of
     * the Square with column number C. */
    private int colCount(int c) {
        int result = 0;
        for (int r = 0; r < BOARD_SIZE; r++) {
            Square s = sq(c, r);
            if (_board[s.index()] != EMP) {
                result++;
            }
        }
        return result;
    }

    /** Return the number of pieces along the horizontal line of
     * the Square with row number R. */
    private int rowCount(int r) {
        int result = 0;
        for (int c = 0; c < BOARD_SIZE; c++) {
            Square s = sq(c, r);
            if (_board[s.index()] != EMP) {
                result++;
            }
        }
        return result;
    }

    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };
    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;

    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;

    /** List of the sizes of continguous clusters of pieces, by color. */
    private final ArrayList<Integer>
        _whiteRegionSizes = new ArrayList<>(),
        _blackRegionSizes = new ArrayList<>();

    /** List of the replaced pieces that helps to track for RETRACT. */
    private ArrayList<Piece> _replaced = new ArrayList<>();


}

