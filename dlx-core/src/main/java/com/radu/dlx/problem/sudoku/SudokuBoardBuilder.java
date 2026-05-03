package com.radu.dlx.problem.sudoku;

import com.radu.dlx.Solver;
import com.radu.dlx.problem.ExactCoverProblemBuilder;

import java.util.List;

/**
 * Generates sudoku items (constraint names) along with options (constraint combinations).
 * It adds only options for unknown sudoku cell values that follow sudoku rules: row, column and block must contain unique cells from 1..9
 */
public final class SudokuBoardBuilder extends ExactCoverProblemBuilder {

    public static SudokuBoardBuilder builder() {
        return new SudokuBoardBuilder();
    }

    public static final int BOARD_SIDE = 9;

    private static final int UNINITIALIZED = -1;
    private static final int BOARD_SIZE = BOARD_SIDE * BOARD_SIDE;
    private static final int BOX_SIDE = 3;
    private static final String[][] rowToConstraint =
            new String[][]{
                    {"r11", "r12", "r13", "r14", "r15", "r16", "r17", "r18", "r19"},
                    {"r21", "r22", "r23", "r24", "r25", "r26", "r27", "r28", "r29"},
                    {"r31", "r32", "r33", "r34", "r35", "r36", "r37", "r38", "r39"},
                    {"r41", "r42", "r43", "r44", "r45", "r46", "r47", "r48", "r49"},
                    {"r51", "r52", "r53", "r54", "r55", "r56", "r57", "r58", "r59"},
                    {"r61", "r62", "r63", "r64", "r65", "r66", "r67", "r68", "r69"},
                    {"r71", "r72", "r73", "r74", "r75", "r76", "r77", "r78", "r79"},
                    {"r81", "r82", "r83", "r84", "r85", "r86", "r87", "r88", "r89"},
                    {"r91", "r92", "r93", "r94", "r95", "r96", "r97", "r98", "r99"}
            };
    private static final String[][] colToConstraint =
            new String[][]{
                    {"c11", "c12", "c13", "c14", "c15", "c16", "c17", "c18", "c19"},
                    {"c21", "c22", "c23", "c24", "c25", "c26", "c27", "c28", "c29"},
                    {"c31", "c32", "c33", "c34", "c35", "c36", "c37", "c38", "c39"},
                    {"c41", "c42", "c43", "c44", "c45", "c46", "c47", "c48", "c49"},
                    {"c51", "c52", "c53", "c54", "c55", "c56", "c57", "c58", "c59"},
                    {"c61", "c62", "c63", "c64", "c65", "c66", "c67", "c68", "c69"},
                    {"c71", "c72", "c73", "c74", "c75", "c76", "c77", "c78", "c79"},
                    {"c81", "c82", "c83", "c84", "c85", "c86", "c87", "c88", "c89"},
                    {"c91", "c92", "c93", "c94", "c95", "c96", "c97", "c98", "c99"}
            };
    private static final String[][] blockToConstraint =
            new String[][]{
                    {"b11", "b12", "b13", "b14", "b15", "b16", "b17", "b18", "b19"},
                    {"b21", "b22", "b23", "b24", "b25", "b26", "b27", "b28", "b29"},
                    {"b31", "b32", "b33", "b34", "b35", "b36", "b37", "b38", "b39"},
                    {"b41", "b42", "b43", "b44", "b45", "b46", "b47", "b48", "b49"},
                    {"b51", "b52", "b53", "b54", "b55", "b56", "b57", "b58", "b59"},
                    {"b61", "b62", "b63", "b64", "b65", "b66", "b67", "b68", "b69"},
                    {"b71", "b72", "b73", "b74", "b75", "b76", "b77", "b78", "b79"},
                    {"b81", "b82", "b83", "b84", "b85", "b86", "b87", "b88", "b89"},
                    {"b91", "b92", "b93", "b94", "b95", "b96", "b97", "b98", "b99"}
            };
    private static final String[][] positionToConstraint =
            new String[][]{
                    {"p11", "p12", "p13", "p14", "p15", "p16", "p17", "p18", "p19"},
                    {"p21", "p22", "p23", "p24", "p25", "p26", "p27", "p28", "p29"},
                    {"p31", "p32", "p33", "p34", "p35", "p36", "p37", "p38", "p39"},
                    {"p41", "p42", "p43", "p44", "p45", "p46", "p47", "p48", "p49"},
                    {"p51", "p52", "p53", "p54", "p55", "p56", "p57", "p58", "p59"},
                    {"p61", "p62", "p63", "p64", "p65", "p66", "p67", "p68", "p69"},
                    {"p71", "p72", "p73", "p74", "p75", "p76", "p77", "p78", "p79"},
                    {"p81", "p82", "p83", "p84", "p85", "p86", "p87", "p88", "p89"},
                    {"p91", "p92", "p93", "p94", "p95", "p96", "p97", "p98", "p99"}
            };
    private final boolean[][] board = new boolean[BOARD_SIDE][BOARD_SIDE];
    private final boolean[][] rows = new boolean[BOARD_SIDE][BOARD_SIDE];
    private final boolean[][] cols = new boolean[BOARD_SIDE][BOARD_SIDE];
    private final boolean[][] blocks = new boolean[BOARD_SIDE][BOARD_SIDE];

    private int loc = UNINITIALIZED;

    private SudokuBoardBuilder() {
    }

    public static SudokuBoardBuilder.SolverBuilder builder(Solver.ProblemAlgoBuilder parent) {
        return new SolverBuilder(parent);
    }

    /**
     * Add elements until the board is filled
     *
     * @param val input value. If value is 0, cells value is undefined, if 1..9 it is set
     * @return true if board is valid
     */
    public boolean addUntilValid(int val) {
        if (val < 0 || val > BOARD_SIDE) {
            throw new IllegalArgumentException("Sudoku cell value must be between 0 and 9: " + val);
        }
        loc++;

        int row = calcRow(loc);
        int col = calcColumn(row, loc);
        int block = calcBlock(row, col);

        String position = positionToConstraint[row][col];

        if (val > 0) {
            int valIdx = val - 1;
            board[row][col] = true;
            rows[row][valIdx] = true;
            cols[col][valIdx] = true;
            blocks[block][valIdx] = true;

            solvedOptions.add(createOption(position, row, col, block, valIdx));
        }
        return isValid();
        /**
         * 0 = "p12"
         * 1 = "r13"
         * 2 = "c23"
         * 3 = "b13"
         */
    }

    private String[] createOption(String position, int row, int col, int block, int valIdx) {
        return new String[]{
                position,
                rowToConstraint[row][valIdx],
                colToConstraint[col][valIdx],
                blockToConstraint[block][valIdx]};
    }

    private int calcRow(int pos) {
        return pos / BOARD_SIDE;
    }

    private int calcColumn(int row, int pos) {
        return (pos - row * 9) % BOARD_SIDE;
    }

    private int calcBlock(int row, int col) {
        return BOX_SIDE * (row / BOX_SIDE) + col / BOX_SIDE;
    }

    @Override
    protected String invalidMessage() {
        return "Board was not filled! Only " + (loc + 1) + " elements were parsed, expected " + BOARD_SIZE;
    }

    @Override
    public void calculateUnknownOptions(List<String[]> options, List<String> items, List<String> secondaryItems) {
        for (int i = 0; i < BOARD_SIDE; i++) {
            for (int k = 0; k < BOARD_SIDE; k++) {
                if (!rows[i][k]) {
                    items.add(rowToConstraint[i][k]);
                }
                if (!cols[i][k]) {
                    items.add(colToConstraint[i][k]);
                }
                if (!blocks[i][k]) {
                    items.add(blockToConstraint[i][k]);
                }
            }
        }

        /*int half = BOARD_SIDE / 2;
        for (int i = 0; i < half; i++) {
            int inc = i + 1;
            for (int k = 0; k < BOARD_SIDE; k++) {
                int upper = half + inc;
                if (upper <= BOARD_SIDE) {
                    if (!rows[upper][k]) {
                        items.advance(rowToConstraint[upper][k]);
                    }
                    if (!cols[upper][k]) {
                        items.advance(colToConstraint[upper][k]);
                    }
                    if (!blocks[upper][k]) {
                        items.advance(blockToConstraint[upper][k]);
                    }
                }
                int lower = half - i;
                if (lower > 0) {
                    if (!rows[lower][k]) {
                        items.advance(rowToConstraint[lower][k]);
                    }
                    if (!cols[lower][k]) {
                        items.advance(colToConstraint[lower][k]);
                    }
                    if (!blocks[lower][k]) {
                        items.advance(blockToConstraint[lower][k]);
                    }
                }
            }
        }*/

        for (int i = 0; i < BOARD_SIDE; i++) {
            for (int j = 0; j < BOARD_SIDE; j++) {
                if (!board[i][j]) {
                    String position = positionToConstraint[i][j];
                    items.add(position);
                    int block = calcBlock(i, j);
                    for (int k = 0; k < BOARD_SIDE; k++) {
                        if (!blocks[block][k]
                                && !rows[i][k]
                                && !cols[j][k]) {
                            options.add(createOption(position, i, j, block, k));
                        }
                    }
                }
            }
        }
    }

    /**
     * zigzag for sudoku as well, like we have for n-queens?
     * private void calculateItems(List<String> items) {
     * int half = n / 2;
     * for (int dec = 0; dec <= n / 2; dec++) {
     * int inc = dec + 1;
     * if (half + inc <= n) {
     * items.advance("r" + (half + inc));
     * items.advance("c" + (half + inc));
     * }
     * if (half - dec > 0) {
     * items.advance("r" + (half - dec));
     * items.advance("c" + (half - dec));
     * }
     * }
     * }
     */

    protected boolean isValid() {
        return loc == BOARD_SIZE - 1;
    }

    public static class SolverBuilder {
        private final Solver.ProblemAlgoBuilder parent;
        private final SudokuBoardBuilder problemBuilder;

        SolverBuilder(Solver.ProblemAlgoBuilder solver) {
            parent = solver;
            problemBuilder = SudokuBoardBuilder.builder();
        }

        public Solver.ProblemAlgoBuilder setBoard(int... cells) {
            if (cells != null && cells.length > 0) {
                boolean valid = false;
                for (int cell : cells) {
                    valid = problemBuilder.addUntilValid(cell);
                    if (valid) {
                        break;
                    }
                }
            }
            return parent.withProblem(problemBuilder.build());
        }
    }
}
