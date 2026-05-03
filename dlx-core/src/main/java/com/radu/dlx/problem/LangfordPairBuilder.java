package com.radu.dlx.problem;

import com.radu.dlx.Solver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Langford pair problem constraint builder.
 * <p>
 * Put $2n$ numbers into $\{1,1,2,2,...,n,n\}$ into $2n$ slots $s_1s_2...s2_{2n}$.
 * Exactly $i$ numbers may be between two occurences of $i$.
 * Thus two occurences of 1 may be one unit apart, two occurences of 2 may be two units apart.
 * <p>
 * This problem was described by C. Dudley Langford "Problem", Mathematical Gazette, 42: 228 (1958).
 * Langford pair construction problem is similar to Skolem sequence construction problem Nordh, Gustav (2008), "Perfect Skolem sets", Discrete Mathematics, 308 (9): 1653–1664 https://arxiv.org/abs/math/0506155
 * <p>
 * Based on D. Knuth's dancing links chapter we have the following definition of Langford pair constraints:
 * <p>
 * $i s_j s_k$ where $1 \leq j \lt k \leq 2n$, $k=i+j+1$m $1\leq\i\leq n$.
 */
public final class LangfordPairBuilder extends ExactCoverProblemBuilder {
    private final int n;

    private LangfordPairBuilder(int problemSize) {
        super();
        n = problemSize;
        if (!isValid()) {
            throw new IllegalStateException(invalidMessage());
        }
    }

    public static ExactCoveringProblem build(int problemSize) {
        return new LangfordPairBuilder(problemSize).build();
    }

    public static SolverBuilder builder(int n, Solver.ProblemAlgoBuilder parent) {
        return new SolverBuilder(n, parent);
    }

    @Override
    protected String invalidMessage() {
        return "Langford pair problem size must be greater than 0: " + n;
    }

    @Override
    protected void calculateUnknownOptions(List<String[]> options, List<String> items, List<String> secondaryItems) {
        Set<String> itemSet = new HashSet<>();
        int maxS = 2 * n; //i = n - [n even] and j > n/2 // i = 10 - 10, i = 11 - 10
        for (int i = 1; i <= n; i++) {
            String item1 = Integer.toString(i);
            if (!itemSet.contains(item1)) {
                items.add(item1);
                itemSet.add(item1);
            }
            for (int right = 3; right <= maxS; right++) {//left j, right k
                int left = right - i - 1;
                if (left > 0 && left < right) {
                    int halfFloor = n / 2;
                    if (!(left > halfFloor && (i == maxS - 2 * halfFloor - 1))) { // other optimization is actually !(i == 1 && left >= n)
                        String item2 = "s" + left;
                        String item3 = "s" + right;
                        if (!itemSet.contains(item2)) {
                            items.add(item2);
                            itemSet.add(item2);
                        }
                        if (!itemSet.contains(item3)) {
                            items.add(item3);
                            itemSet.add(item3);
                        }
                        options.add(new String[]{item1, item2, item3});
                    }
                }
            }
        }
    }

    @Override
    protected boolean isValid() {
        return n > 0;
    }

    public static class SolverBuilder {

        private final Solver.ProblemAlgoBuilder parent;

        public SolverBuilder(int n, Solver.ProblemAlgoBuilder algoBuilder) {
            parent = algoBuilder.withProblem(new LangfordPairBuilder(n).build());

        }

        public Solver.ProblemAlgoBuilder forAlgorithm() {
            return parent;
        }
    }

}
