package com.radu.dlx.io.storer;

import com.radu.dlx.io.tree.SolutionTree;
import com.radu.dlx.struct.DancingStructure;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Solutions will be stored in a binary format as two files.
 * One file will contain the index and the other file will contain the solutions.
 * <p>
 * The files, if they exists, will be truncated and access pattern will be append or read only.
 * Currently the storer cannot be shared by threads and should only be accessed by one thread.
 * <p>
 * Memory structure of the index:
 * <p>
 * |LIST_COUNT 4 bytes = LIST count N| LIST 0 POS OFFSET l_0_0 4 bytes| LIST 2 POS OFFSET list_2_0 4 bytes | ... | LIST_ {N-1} POS OFFSET l_{N-1}_0|
 * <p>
 * Memory structure of the solution data:
 * <p>
 * |LIST l_0 LEN 4 bytes = LIST len M | ELEM l_0_0 4 bytes | ... | ELEM l_0_{M-1} 4 bytes|
 * ...
 * |LIST l_{N-1} LEN 4 bytes = LIST len M_{N-1}| ELEM l_{N-1}_0 4 bytes | ... | ELEM l_{N-1}_{M - 1} 4 bytes|
 * <p>
 * Currently we do not utilize any compression or mathematical properties of {@link DancingStructure} for reducing file size.
 * If we were to go concurrent, we can assign pages for each thread. Also we can use unsigned integers.
 * <p>
 * If each solution has 10 items, our file memory consumption will be for 1 000 000 000 solutions, where each solution has 10 options:
 * Index: 4 + 1 000 000 000 * 4 = 4 000 000 004 bytes
 * Solution: 1 000 000 000 * (4 + 10*4) =  44 000 000 000 bytes
 * <p>
 * For smaller problems, where we can be sure that the max solution option index is <=2^16 - 1 and solution list can only have <=2^8 - 1 elements,
 * we can use a byte for list length and a short for option index.
 * <p>
 * If each solution has 10 items, our file memory consumption will be for 1 000 000 000 solutions, where each solution has 10 options:
 * * Index: 4 + 1 000 000 000 * 4 = 4 000 000 004 bytes
 * * Solution: 1 000 000 000 * (1 + 10*2) =  21 000 000 000 bytes
 * <p>
 * Parallelization idea:
 * store the memory state of best branching point and send these points along with the branch id to a thread
 */
public class MemoryMappedFileIntStorer implements SolutionStorer, AutoCloseable {
    private static final int PAGE_SIZE = 1_000_000;//1MB page as minimum map size
    private final RandomAccessFile solutionFile;
    private final List<Integer> solutionReadPos = new ArrayList<>();
    private final RandomAccessFile indexFile;
    private final FileChannel solutionChannel;
    private final FileChannel indexChannel;
    private MappedByteBuffer solutionBuff;
    private MappedByteBuffer indexBuff;
    private int solutionPosition;
    private int solutionBuffStart;
    private int indexBuffStart;
    private final int listCountPosition = 0;
    private int solutionCount;

    public MemoryMappedFileIntStorer(SolutionStorer storer) throws IOException {
        solutionFile = new RandomAccessFile("calc.out", "rwd");
        solutionFile.setLength(0L);//we reset the file
        solutionChannel = solutionFile.getChannel();
        solutionChannel.position(0);
        solutionBuff = solutionChannel.map(FileChannel.MapMode.READ_WRITE, 0, PAGE_SIZE);

        indexFile = new RandomAccessFile("calc.idx", "rwd");
        indexFile.setLength(0L);//we reset the file
        indexChannel = indexFile.getChannel();
        indexChannel.position(0);
        indexBuff = indexChannel.map(FileChannel.MapMode.READ_WRITE, 0, PAGE_SIZE);

        solutionPosition = 0;
        solutionBuffStart = 0;
        indexBuffStart = 0;
        solutionCount = 0;
        updateIndexHeader();

        storer.getSolutions().forEach(this::storeSolution);
    }

    private void updateIndexHeader() {
        putIndexInt(listCountPosition, solutionCount);
    }

    private void updateIndex(int solutionIndex, int solutionOffset) {
        putIndexInt(indexPosition(solutionIndex), solutionOffset);
    }

    private int indexPosition(int solutionIndex) {
        return Integer.BYTES + solutionIndex * Integer.BYTES;
    }

    private void storeSolution(int[] solution) {
        int offset = solutionPosition;
        solutionReadPos.add(offset);
        updateIndex(solutionCount, offset);

        putSolutionInt(solutionPosition, solution.length);
        solutionPosition += Integer.BYTES;
        for (int val : solution) {
            putSolutionInt(solutionPosition, val);
            solutionPosition += Integer.BYTES;
        }

        solutionCount++;
        updateIndexHeader();
    }

    private void putSolutionInt(int position, int value) {
        solutionBuff = ensureMapped(solutionChannel, solutionBuff, position, true);
        solutionBuff.putInt(position - solutionBuffStart, value);
    }

    private void putIndexInt(int position, int value) {
        indexBuff = ensureMapped(indexChannel, indexBuff, position, false);
        indexBuff.putInt(position - indexBuffStart, value);
    }

    private int readSolutionInt(int position) {
        solutionBuff = ensureMapped(solutionChannel, solutionBuff, position, true);
        return solutionBuff.getInt(position - solutionBuffStart);
    }

    private int readIndexInt(int position) {
        indexBuff = ensureMapped(indexChannel, indexBuff, position, false);
        return indexBuff.getInt(position - indexBuffStart);
    }

    private MappedByteBuffer ensureMapped(FileChannel channel, MappedByteBuffer buffer, int position, boolean solution) {
        int start = solution ? solutionBuffStart : indexBuffStart;
        if (position >= start && position + Integer.BYTES <= start + PAGE_SIZE) {
            return buffer;
        }
        int newStart = position - position % PAGE_SIZE;
        try {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.READ_WRITE, newStart, PAGE_SIZE);
            if (solution) {
                solutionBuffStart = newStart;
            } else {
                indexBuffStart = newStart;
            }
            return mapped;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot map solution storage file", e);
        }
    }

    private int[] readSolution(int solutionIndex) {
        int position = solutionReadPos.size() > solutionIndex
                ? solutionReadPos.get(solutionIndex)
                : readIndexInt(indexPosition(solutionIndex));
        int len = readSolutionInt(position);
        int[] solution = new int[len];
        int current = position + Integer.BYTES;
        for (int i = 0; i < len; i++) {
            solution[i] = readSolutionInt(current);
            current += Integer.BYTES;
        }
        return solution;
    }

    @Override
    public void store(SolutionTree tree) {
        storeSolution(tree.getActiveBranch());
    }

    @Override
    public Stream<int[]> getSolutions() {
        return IntStream.range(0, solutionCount).mapToObj(this::readSolution);
    }

    @Override
    public int[] getFirstSolution() {
        if (solutionCount == 0) {
            return new int[]{};
        }
        return readSolution(0);
    }

    @Override
    public int getSolutionCount() {
        return solutionCount;
    }

    @Override
    public void close() throws IOException {
        solutionBuff.force();
        indexBuff.force();
        solutionChannel.close();
        indexChannel.close();
        solutionFile.close();
        indexFile.close();
    }
}
