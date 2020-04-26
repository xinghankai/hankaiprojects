/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;




import java.util.List;

import static loa.Piece.*;

/** An automated Player.
 *  @author Hankai Xing
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {
            value = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (depth == 0) {
            return heuriScore(board);
        } else if (board.winner() == WP) {
            return WINNING_VALUE;
        } else if (board.winner() == BP) {
            return -WINNING_VALUE;
        }
        int bestScore;
        if (sense == 1) {
            bestScore = -INFTY;
        } else {
            bestScore = INFTY;
        }
        for (Move m: board.legalMoves()) {
            if (sense == 1) {
                if (saveMove && _foundMove == null) {
                    _foundMove = m;
                }
                Board tmp = new Board(board);
                tmp.makeMove(m);
                int score = findMove(tmp,
                        depth - 1, false, -sense, alpha, beta);
                if (score > bestScore) {
                    if (saveMove) {
                        _foundMove = m;
                    }
                    bestScore = score;
                }
                alpha = Math.max(score, alpha);
            } else {
                if (saveMove && _foundMove == null) {
                    _foundMove = m;
                }
                Board tmp = new Board(board);
                tmp.makeMove(m);
                int score = findMove(tmp,
                        depth - 1, false, -sense, alpha, beta);
                if (score < bestScore) {
                    if (saveMove) {
                        _foundMove = m;
                    }
                    bestScore = score;
                }
                beta = Math.min(score, beta);
            }
            if (alpha >= beta) {
                break;
            }
        }
        return bestScore;
    }

    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 1;
    }

    /** Calculate my heuriScore with a board BOARD, returns a INT. */
    private int heuriScore(Board board) {
        int value;
        List<Integer> wSideContig = board.getRegionSizes(WP);
        List<Integer> bSideContig = board.getRegionSizes(BP);
        value = 100 * bSideContig.size() / wSideContig.size();
        return value;
    }

    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;

}
