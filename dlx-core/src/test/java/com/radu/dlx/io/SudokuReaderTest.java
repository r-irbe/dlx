package com.radu.dlx.io;

import com.radu.dlx.problem.ExactCoveringProblem;
import com.radu.dlx.problem.sudoku.SudokuBoardBuilder;
import com.radu.dlx.problem.sudoku.SudokuBoardReader;
import com.radu.dlx.problem.sudoku.SudokuBoardWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SudokuReaderTest {

    @Test
    public void whenReadingNonCellCharacters_boardIsEmpty() {
        final ByteArrayInputStream in = new ByteArrayInputStream("".getBytes());
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> SudokuBoardReader.create().read(in));
        assertEquals("Board was not filled! Only 0 elements were parsed, expected 81", thrown.getMessage());
    }

    @Test
    public void whenAddingOutOfRangeCellValue_throw() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> SudokuBoardBuilder.builder().addUntilValid(10));
        assertEquals("Sudoku cell value must be between 0 and 9: 10", thrown.getMessage());
    }

    @Test
    public void whenReadingEmptyCellCharacters_boardIsEmpty() {
        testSudokuReader("0".repeat(81), "000000000\n000000000\n000000000\n000000000\n" +
                "000000000\n000000000\n000000000\n000000000\n000000000", 324, 0, 729);
    }

    @Test
    public void whenReadingCellsOfOneBlock_boardHasThatBlock() {
        testSudokuReader("123" + "0".repeat(6) +
                "456" + "0".repeat(6) +
                "789" + "0".repeat(6) +
                "0".repeat(54), "123000000\n" +
                "456000000\n" +
                "789000000\n" +
                "000000000\n" +
                "000000000\n" +
                "000000000\n" +
                "000000000\n" +
                "000000000\n" +
                "000000000", 288, 9, 540);
    }

    private void testSudokuReader(String input, String expectedBoard, int items, int knownOptions, int options) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ExactCoveringProblem board = SudokuBoardReader.create().read(in);

        assertEquals(items, board.items.size());
        assertEquals(knownOptions, board.knownOptions.size());
        assertEquals(options, board.options.size());
        //assertEquals(knownItems, board.knownItems.size());
        assertEquals(expectedBoard, SudokuBoardWriter.create().print(board));
    }
}
