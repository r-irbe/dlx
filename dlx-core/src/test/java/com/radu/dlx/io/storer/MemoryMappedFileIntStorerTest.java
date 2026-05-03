package com.radu.dlx.io.storer;

import com.radu.dlx.io.tree.SolutionTree;
import com.radu.dlx.struct.DancingStructure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MemoryMappedFileIntStorerTest {

    @AfterEach
    public void cleanup() throws IOException {
        Files.deleteIfExists(Paths.get("calc.out"));
        Files.deleteIfExists(Paths.get("calc.idx"));
    }

    @Test
    public void whenStoringSolutions_canReadThemBack() throws IOException {
        try (MemoryMappedFileIntStorer storer = new MemoryMappedFileIntStorer(new InMemoryStorer())) {

            storer.store(new TestSolutionTree(new int[]{3, 5, 8}));
            storer.store(new TestSolutionTree(new int[]{13, 21}));

            assertEquals(2, storer.getSolutionCount());
            assertArrayEquals(new int[]{3, 5, 8}, storer.getFirstSolution());
            List<String> solutions = storer.getSolutions().map(Arrays::toString).collect(Collectors.toList());
            assertEquals("[3, 5, 8]", solutions.get(0));
            assertEquals("[13, 21]", solutions.get(1));
        }
    }

    @Test
    public void whenInitializedFromExistingStorer_copiesSolutions() throws IOException {
        InMemoryStorer source = new InMemoryStorer();
        source.store(new TestSolutionTree(new int[]{1, 2}));

        try (MemoryMappedFileIntStorer storer = new MemoryMappedFileIntStorer(source)) {

            assertEquals(1, storer.getSolutionCount());
            assertArrayEquals(new int[]{1, 2}, storer.getFirstSolution());
        }
    }

    private static class TestSolutionTree implements SolutionTree {
        private final int[] activeBranch;

        TestSolutionTree(int[] activeBranch) {
            this.activeBranch = activeBranch;
        }

        @Override
        public int currentOption() { return 0; }

        @Override
        public int currentItem() { return 0; }

        @Override
        public int level() { return 0; }

        @Override
        public int size() { return 0; }

        @Override
        public int maxLevel() { return 0; }

        @Override
        public int maxSolutionNum() { return 0; }

        @Override
        public boolean isEmpty() { return false; }

        @Override
        public int[] getActiveBranch() { return activeBranch; }

        @Override
        public void storeSolution() { }

        @Override
        public int getSolutionCount() { return 0; }

        @Override
        public int[] getFirstSolution() { return new int[]{}; }

        @Override
        public double completionScore() { return 0; }

        @Override
        public void advance(int value) { }

        @Override
        public int backup() { return 0; }

        @Override
        public boolean isDone() { return false; }

        @Override
        public Stream<String> printCurrentSolutions(DancingStructure struct) { return Stream.empty(); }

        @Override
        public Stream<List<String>> printCurrentSolutionsAsList(DancingStructure struct) { return Stream.empty(); }
    }
}
