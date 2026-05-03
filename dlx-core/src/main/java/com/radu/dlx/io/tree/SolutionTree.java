package com.radu.dlx.io.tree;

import com.radu.dlx.struct.DancingStructure;

import java.util.List;
import java.util.stream.Stream;

/**
 * TODO: We do not expose storers or storing strategies out side of the solution tree, as this is not essential for the users at the moment.
 */
public interface SolutionTree {
    int currentOption();

    int currentItem();

    int level();

    int size();

    int maxLevel();

    int maxSolutionNum();//TODO: do we need this? We can keep this in SolverConfig

    /**
     * @return true when the solution tree has never advanced from the root.
     * This does not necessarily mean that the current active branch is empty after backtracking.
     */
    boolean isEmpty();

    /**
     * @return Get current active branch. Returned array is threadsafe and not shared.
     */
    int[] getActiveBranch();

    void storeSolution();

    int getSolutionCount();

    int[] getFirstSolution();

    /* * TODO: After all solutions are stored, completion score should be 1!
     * TODO: Should the solution score be displayed in toString()?
     */
    double completionScore();

    /**
     * Add an val (option) to solution tree.
     * <p>
     * If val is added in the middle of the solution tree, a new path will be created and the subtree that is removed
     * from the valid path, will be moved to an invalid relation from a valid relation and the new val will be set
     *
     * @param value option
     */
    void advance(int value);

    int backup();

    boolean isDone();

    Stream<String> printCurrentSolutions(DancingStructure struct);

    String toString();

    Stream<List<String>> printCurrentSolutionsAsList(DancingStructure struct);
}
