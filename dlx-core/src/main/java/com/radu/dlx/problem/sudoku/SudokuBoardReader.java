package com.radu.dlx.problem.sudoku;

import com.radu.dlx.problem.ExactCoveringProblem;

import java.io.IOException;
import java.io.InputStream;
import java.util.PrimitiveIterator;

/**
 * Class for reading the sudoku specification from an input stream
 */
public final class SudokuBoardReader {
    static final int NUM_ZERO_CHAR_CODE = 48;
    private static final int NUM_NINE_CHAR_CODE = 57;
    private static final String CONTROL_CHAR = "\\Z";
    private static final char DEFAULT_EMPTY_CELL = NUM_ZERO_CHAR_CODE;
    private final char emptyCell;

    private SudokuBoardReader(char emptyCell) {
        this.emptyCell = emptyCell;
    }

    public static SudokuBoardReader create(char emptyCell) {
        return new SudokuBoardReader(emptyCell);
    }

    public static SudokuBoardReader create() {
        return new SudokuBoardReader(DEFAULT_EMPTY_CELL);
    }

    public ExactCoveringProblem read(InputStream in) {
        return read(streamToString(in));
    }

    public ExactCoveringProblem read(String inStr) {
        SudokuBoardBuilder builder = SudokuBoardBuilder.builder();

        PrimitiveIterator.OfInt chars = inStr.codePoints().iterator();
        while (chars.hasNext()) {
            int chr = chars.nextInt();
            if (isNumber(chr)
                    && builder.addUntilValid(chrToNum(chr))) break;
        }
        return builder.build();
    }

    private boolean isNumber(int chr) {
        return chr == emptyCell || (chr >= NUM_ZERO_CHAR_CODE && chr <= NUM_NINE_CHAR_CODE);
    }

    private int chrToNum(int chr) {
        if(chr == emptyCell) {
            return 0;
        }
        return chr - NUM_ZERO_CHAR_CODE;
    }

    /**
     * We need to only read the file input stream up to a control character
     * We will not be closing the input stream, as that is the responsibility of the caller.
     * In case of Standard input stream, JVM and OS has to handle its closing
     */
    private String streamToString(InputStream in) {
        try {
            return new String(in.readAllBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read sudoku input", e);
        }
    }
}
