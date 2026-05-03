package com.radu.dlx.problem;

import com.radu.dlx.Solver;

import java.util.List;

/**
 * NQueen problem -> place one queen exactly on every row and on every column
 * <p>
 * Encoded by formulas:
 * $\sum_{i=1}^n x_{ij}$ where $1\leq j \leq n$
 * $\sum_{j=1}^n x_{ij}$ where $1\leq i \leq n$
 * $\sum\lbrace x_{ij} | 1 \leq i,j \leq n, i + j = s \rbrace + u_s = 1$ where $1 < s \leq 2 n$ -- upward diagonal
 * $\sum\lbrace x_{ij} | 1 \leq i,j \leq n, i - j = d \rbrace + v_d = 1$ where $-n < d < n$     -- downward diagonal
 * <p>
 * results in n^2 + 4*n - 2 variables
 */
public final class NQueenProblem extends ExactCoverProblemBuilder {
    private final int n;
    private final boolean[] udArr;
    private final boolean[] ddArr;

    private NQueenProblem(int problemSize) {
        n = problemSize;
        udArr = new boolean[2 * problemSize + 1];
        ddArr = new boolean[2 * problemSize + 1];
        if (!isValid()) {
            throw new IllegalStateException(invalidMessage());
        }
    }

    public static ExactCoveringProblem build(int problemSize) {
        return new NQueenProblem(problemSize).build();
    }

    public static NQueenProblem.SolverBuilder builder(int n, Solver.ProblemAlgoBuilder parent) {
        return new NQueenProblem.SolverBuilder(n, parent);
    }

    @Override
    protected String invalidMessage() {
        return "NQueen problem size must be greater than 0: " + n;
    }

    @Override
    protected void calculateUnknownOptions(List<String[]> options, List<String> items, List<String> secondaryItems) {
        calculateItems(items);
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                /*
                 * row -- row
                 * col -- column
                 * ud -- down to up diagnoal
                 * dd -- up to down diagonal
                 */
                String row = "r" + i;
                String col = "c" + j;
                int ud = i + j;
                String t = "t" + ud;
                if (!udArr[ud]) {
                    secondaryItems.add(t);
                    udArr[ud] = true;
                }
                int dd = i - j;
                String u = "u" + dd;
                if (!ddArr[dd + n]) {
                    secondaryItems.add(u);
                    ddArr[dd + n] = true;
                }
                options.add(new String[]{row, col, t, u});
            }
        }
    }


    @Override
    protected boolean isValid() {
        return n > 0;
    }

    /**
     * Here we are inserting items in a zig-zag order from the center of the board
     */
    private void calculateItems(List<String> items) {
        int half = n / 2;
        for (int dec = 0; dec <= n / 2; dec++) {
            int inc = dec + 1;
            if (half + inc <= n) {
                items.add("r" + (half + inc));
                items.add("c" + (half + inc));
            }
            if (half - dec > 0) {
                items.add("r" + (half - dec));
                items.add("c" + (half - dec));
            }
        }
    }

    public static class SolverBuilder {

        private final Solver.ProblemAlgoBuilder parent;

        public SolverBuilder(int n, Solver.ProblemAlgoBuilder algoBuilder) {
            parent = algoBuilder.withProblem(new NQueenProblem(n).build());

        }

        public Solver.ProblemAlgoBuilder forAlgorithm() {
            return parent;
        }
    }

}
