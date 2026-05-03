package com.radu.dlx.algo;

import com.radu.dlx.io.progress.TeeTrackingStream;
import com.radu.dlx.io.progress.TrackingStream;
import com.radu.dlx.io.tree.OfIntSolutionTree;
import com.radu.dlx.io.tree.SolutionTree;
import com.radu.dlx.problem.ExactCoveringProblem;
import com.radu.dlx.problem.LangfordPairBuilder;
import com.radu.dlx.problem.NQueenProblem;
import com.radu.dlx.struct.DancingArray;
import com.radu.dlx.struct.DancingArrayBuilder;
import com.radu.dlx.struct.DancingArrays;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DancingLinksArrayStacklessTest {
    @Test
    public void whenSolvingExactCover_firstBacktrackCorrect() {
        DancingArray array = DancingArrayBuilder.buildExactCover();
        DancingLinks algo = DancingLinks.create(OfIntSolutionTree.createStoring(array, 1), array);
        algo.forward();
        algo.advance();
        algo.forward();
        algo.advance();
        algo.forward();
        algo.advance();
        assertEquals(
                "    i      0     1     2     3     4     5     6     7     8   \n" +
                        " name(i)         a     b     c     d     e     f     g         \n" +
                        "llink(i)   5     0     0     0     3     0     5     6     8   \n" +
                        "rlink(i)   5     2     3     5     5     0     0     0     8   \n" +
                        "    x      0     1     2     3     4     5     6     7     8   \n" +
                        " len(x)    0     2     1     1     1     0     0     1     0   \n" +
                        "ulink(x)   0     20    16    9     27    5     6     25    0   \n" +
                        "dlink(x)   0     12    16    9     27    5     6     25    10  \n" +
                        "    x      9     10    11    12    13    14    15    16    17  \n" +
                        " top(x)    3     5     -1    1     4     7     -2    2     3   \n" +
                        "ulink(x)   3     5     9     1     4     7     12    2     9   \n" +
                        "dlink(x)   3     5     14    20    21    25    18    2     3   \n" +
                        "    x      18    19    20    21    22    23    24    25    26  \n" +
                        " top(x)    6     -3    1     4     6     -4    2     7     -5  \n" +
                        "ulink(x)   6     16    12    4     18    20    16    7     24  \n" +
                        "dlink(x)   6     22    1     27    6     25    2     7     29  \n" +
                        "    x      27    28    29    30  \n" +
                        " top(x)    4     5     7     -6  \n" +
                        "ulink(x)   4     10    25    27  \n" +
                        "dlink(x)   4     5     7     0   \n",
                array.toString());
    }

    @Test
    public void whenSolvingExactCover_firstRecoveryCorrect() {
        DancingArray array = DancingArrayBuilder.buildExactCover();
        DancingLinks algo = DancingLinks.create(OfIntSolutionTree.createStoring(array, 1), array);
        algo.forward();
        algo.advance();
        algo.forward();
        algo.advance();
        algo.forward();
        algo.advance();
        algo.recover();
        assertEquals(
                "    i      0     1     2     3     4     5     6     7     8   \n" +
                        " name(i)         a     b     c     d     e     f     g         \n" +
                        "llink(i)   6     0     0     0     3     3     5     6     8   \n" +
                        "rlink(i)   3     2     3     5     5     6     0     0     8   \n" +
                        "    x      0     1     2     3     4     5     6     7     8   \n" +
                        " len(x)    0     2     1     1     1     1     0     1     0   \n" +
                        "ulink(x)   0     20    16    9     27    10    6     25    0   \n" +
                        "dlink(x)   0     12    16    9     27    10    6     25    10  \n" +
                        "    x      9     10    11    12    13    14    15    16    17  \n" +
                        " top(x)    3     5     -1    1     4     7     -2    2     3   \n" +
                        "ulink(x)   3     5     9     1     4     7     12    2     9   \n" +
                        "dlink(x)   3     5     14    20    21    25    18    2     3   \n" +
                        "    x      18    19    20    21    22    23    24    25    26  \n" +
                        " top(x)    6     -3    1     4     6     -4    2     7     -5  \n" +
                        "ulink(x)   6     16    12    4     18    20    16    7     24  \n" +
                        "dlink(x)   6     22    1     27    6     25    2     7     29  \n" +
                        "    x      27    28    29    30  \n" +
                        " top(x)    4     5     7     -6  \n" +
                        "ulink(x)   4     10    25    27  \n" +
                        "dlink(x)   4     5     7     0   \n",
                array.toString());
    }

    @Test
    public void shouldSolveExactCover() {
        DancingArray array = DancingArrayBuilder.buildExactCover();
        SolutionTree solution = DancingLinks.create(OfIntSolutionTree.createStoring(array, 2), array).solve();
        Optional<String> solutions = solution.printCurrentSolutions(array).findFirst();
        assertEquals("3:a->d->f,4:b->g,0:c->e", solutions.get());
        assertEquals(DancingArrayBuilder.INITIAL_EXACT_COVER, array.toString());
        assertEquals(1, solution.getSolutionCount());
    }

    @Test
    public void shouldSolveLangfordPairN3Memory() {
        DancingArray array = DancingArrayBuilder.buildLangfordPairN3();
        assertEquals(
                "    i      0     1     2     3     4     5     6     7     8     9     10  \n" +
                        " name(i)         1     2     3     s1    s2    s3    s4    s5    s6        \n" +
                        "llink(i)   9     0     1     2     3     4     5     6     7     8     10  \n" +
                        "rlink(i)   1     2     3     4     5     6     7     8     9     0     10  \n" +
                        "    x      0     1     2     3     4     5     6     7     8     9     10  \n" +
                        " len(x)    0     4     3     2     3     3     3     3     3     3     0   \n" +
                        "ulink(x)   0     23    35    43    40    44    36    29    41    45    0   \n" +
                        "dlink(x)   0     11    27    39    12    16    13    17    21    25    13  \n" +
                        "    x      11    12    13    14    15    16    17    18    19    20    21  \n" +
                        " top(x)    1     4     6     -1    1     5     7     -2    1     6     8   \n" +
                        "ulink(x)   1     4     6     11    11    5     7     15    15    13    8   \n" +
                        "dlink(x)   15    28    20    17    19    32    24    21    23    36    33  \n" +
                        "    x      22    23    24    25    26    27    28    29    30    31    32  \n" +
                        " top(x)    -3    1     7     9     -4    2     4     7     -5    2     5   \n" +
                        "ulink(x)   19    19    17    9     23    2     12    24    27    27    16  \n" +
                        "dlink(x)   25    1     29    37    29    31    40    7     33    35    44  \n" +
                        "    x      33    34    35    36    37    38    39    40    41    42    43  \n" +
                        " top(x)    8     -6    2     6     9     -7    3     4     8     -8    3   \n" +
                        "ulink(x)   21    31    31    20    25    35    3     28    33    39    39  \n" +
                        "dlink(x)   41    37    2     6     45    41    43    4     8     45    3   \n" +
                        "    x      44    45    46  \n" +
                        " top(x)    5     9     -9  \n" +
                        "ulink(x)   32    37    43  \n" +
                        "dlink(x)   5     9     0   \n",
                array.toString());
        SolutionTree solution = DancingLinks.create(OfIntSolutionTree.createStoring(array, 2), array).solve();
        List<String> solutions = solution.printCurrentSolutions(array).collect(Collectors.toList());
        assertEquals("7:3->s1->s5,6:2->s3->s6,1:1->s2->s4", solutions.get(0));
        assertEquals("8:3->s2->s6,4:2->s1->s4,2:1->s3->s5", solutions.get(1));
        assertEquals(2, solutions.size());
    }

    @Test
    public void shouldSolveLangfordPairN3() {
        DancingArray array = DancingArrays.create(LangfordPairBuilder.build(3));
        SolutionTree solution = DancingLinks.create(OfIntSolutionTree.createStoring(array, 1000), array).solve();
        assertEquals(1, solution.getSolutionCount());
    }

    @Test
    public void whenBuildingInvalidLangfordPair_throwWithMessage() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> LangfordPairBuilder.build(0));
        assertEquals("Langford pair problem size must be greater than 0: 0", thrown.getMessage());
    }

    @Test
    public void shouldSolveLangfordPairN7() {
        DancingArray array = DancingArrays.create(LangfordPairBuilder.build(7));//DancingarrayBuilder.buildLangfordPairN3();
        SolutionTree solution = DancingLinks.create(OfIntSolutionTree.createStoring(array, 1000), array).solve();
        assertEquals(26, solution.getSolutionCount());
    }

    @Test
    public void shouldSolveLangfordPairN8() {
        DancingArray array = DancingArrays.create(LangfordPairBuilder.build(8));//DancingarrayBuilder.buildLangfordPairN3();
        SolutionTree solution = DancingLinks.create(OfIntSolutionTree.createStoring(array, 1000), array).solve();
        assertEquals(150, solution.getSolutionCount()); //symmetrical solutions
    }

    @Test
    public void shouldSolve4Queens() {
        ExactCoveringProblem problem = NQueenProblem.build(4);
        TrackingStream stream = new TeeTrackingStream();
        stream.register(val -> System.out.println(val.print()));
        DancingArray array = DancingArrays.create(problem);//DancingarrayBuilder.buildLangfordPairN3();
        assertEquals("    i      0     1     2     3     4     5     6     7     8     9     10    11    12    13    14    15    16    17    18    19    20    21    22    23  \n" +
                " name(i)         r3    c3    r2    c2    r4    c4    r1    c1    t2    u0    t3   u-1    t4   u-2    t5   u-3    u1    t6    u2    t7    u3    t8        \n" +
                "llink(i)   8     0     1     2     3     4     5     6     7     23    9     10    11    12    13    14    15    16    17    18    19    20    21    22  \n" +
                "rlink(i)   1     2     3     4     5     6     7     8     0     10    11    12    13    14    15    16    17    18    19    20    21    22    23    9   \n" +
                "    x      0     1     2     3     4     5     6     7     8     9     10    11    12    13    14    15    16    17    18    19    20    21    22    23  \n" +
                " len(x)    0     4     4     4     4     4     4     4     4     1     4     2     3     3     2     4     1     3     3     2     2     1     1     0   \n" +
                "ulink(x)   0     79    95    59    90    99   100    39    85    26   102    46    82    66    62    86    42    97    91    92    96    87   101    0   \n" +
                "dlink(x)   0     64    35    44    30    84    40    24    25    26    27    31    32    36    37    41    42    47    61    67    81    87   101    27  \n" +
                "    x      24    25    26    27    28    29    30    31    32    33    34    35    36    37    38    39    40    41    42    43    44    45    46    47  \n" +
                " top(x)    7     8     9     10    -1    7     4     11    12    -2    7     2     13    14    -3    7     6     15    16    -4    3     8     11    17  \n" +
                "ulink(x)   7     8     9     10    24    24    4     11    12    29    29    2     13    14    34    34    6     15    16    39    3     25    31    17  \n" +
                "dlink(x)   29    45    9     52    32    34    50    46    57    37    39    55    51    62    42    7     60    56    16    47    49    65    11    72  \n" +
                "    x      48    49    50    51    52    53    54    55    56    57    58    59    60    61    62    63    64    65    66    67    68    69    70    71  \n" +
                " top(x)    -5    3     4     13    10    -6    3     2     15    12    -7    3     6     18    14    -8    1     8     13    19    -9    1     4     15  \n" +
                "ulink(x)   44    44    30    36    27    49    49    35    41    32    54    54    40    18    37    59    1     45    51    19    64    64    50    56  \n" +
                "dlink(x)   52    54    70    66    77    57    59    75    71    82    62    3     80    76    14    67    69    85    13    92    72    74    90    86  \n" +
                "    x      72    73    74    75    76    77    78    79    80    81    82    83    84    85    86    87    88    89    90    91    92    93    94    95  \n" +
                " top(x)    17   -10    1     2     18    10   -11    1     6     20    12   -12    5     8     15    21   -13    5     4     18    19   -14    5     2   \n" +
                "ulink(x)   47    69    69    55    61    52    74    74    60    20    57    79    5     65    71    21    84    84    70    76    67    89    89    75  \n" +
                "dlink(x)   97    77    79    95    91   102    82    1    100    96    12    87    89    8     15    21    92    94    4     18    19    97    99    2   \n" +
                "    x      96    97    98    99   100   101   102   103  \n" +
                " top(x)    20    17   -15    5     6     22    10   -16  \n" +
                "ulink(x)   81    72    94    94    80    22    77    99  \n" +
                "dlink(x)   20    17   102    5     6     22    10    0   \n", array.toString());

        DancingLinks algo = DancingLinks.createVebose(OfIntSolutionTree.createStoring(array, 1000), array, stream);
        SolutionTree solution = algo.solve();

        assertEquals("        [root]        \n" +
                        "                      ->[11:r3->c4->t7->u-1]->[10:r3->c3->t6->u0]  ->[9:r3->c2->t5->u1]  ->[8:r3->c1->t4->u2] \n" +
                        "                                            ->[2:r1->c3->t4->u-2]  ->[4:r2->c1->t3->u1] ->[2:r1->c3->t4->u-2] ->[1:r1->c2->t3->u-1] \n" +
                        "                                                                   ->[4:r2->c1->t3->u1] ->[14:r4->c3->t7->u1] \n" +
                        "                                                                                        ->[13:r4->c2->t6->u2] ->[7:r2->c4->t6->u-2] \n",
                solution.toString());
        assertEquals(2, solution.getSolutionCount());
        List<String> solutions = solution.printCurrentSolutions(array).collect(Collectors.toList());
        assertEquals("8:r3->c1->t4->u2,1:r1->c2->t3->u-1,14:r4->c3->t7->u1,7:r2->c4->t6->u-2", solutions.get(0));
        assertEquals("11:r3->c4->t7->u-1,2:r1->c3->t4->u-2,4:r2->c1->t3->u1,13:r4->c2->t6->u2", solutions.get(1));
    }

    @Test
    public void whenBuildingInvalidNQueen_throwWithMessage() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> NQueenProblem.build(0));
        assertEquals("NQueen problem size must be greater than 0: 0", thrown.getMessage());
    }

}
