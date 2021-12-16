package org.kiva.sudoku;

import org.kiva.common.utils.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CLI to parse a CSV 9x9 grid of numbers and valid the grid using Sudoku's rules.
 * Typically, this is a 9x9 grid. Rule for a 9x9 grid are as follows
 * - Contain every number from 1 to 9 exactly once in each row.
 * - Contain every number from 1 to 9 exactly once in each column.
 * - Contain every number from 1 to 9 exactly once in each of nine 3x3 sub-grids
 */
public class Sudoku {
    static int AXIS_SIZE = 9;
    static int BLOCK_SIZE = 3;

    private final List<int[]> test;
    private ArrayList<Pair<Integer, Integer>> blockMap;

    /**
     * Takes CSV file, validates the content, and prepare to evaluate it.
     *
     * @param csvFile
     * @throws IOException
     */
    public Sudoku(File csvFile) throws IOException {
        test = Files.lines(Paths.get(csvFile.getAbsolutePath())).filter(line -> line.length() > 0) // Remove empty lines
                .map(line -> line.split("(,)")) // Split on commas
                .map(r -> Arrays.stream(r).mapToInt(Integer::parseInt).toArray()) // Convert each row to integers, this may throw if not a number
                .collect(Collectors.toList()); // Store in a list

        // Validate the CSV input
        validateInput();
        // build a row/colum indices for building a block
        buildBoxIndices();
    }

    /**
     * CLI entry point. takes one argument; the CSV file
     *
     * @param args
     */
    public static void main(String[] args) {
        File filename = null;

        // no args, print usage and exit
        if (args.length == 0) {
            System.out.println("Usage: java -jar suduko.jar </path/to/CSV/file>");
            System.exit(-1);
        }


        // Should be the CSV file to validate
        filename = new File(args[0]);

        // If the file does not exist or is not a file, print error and exist
        if (!filename.exists() || !filename.isFile()) {
            System.out.println("Usage: java -jar suduko.jar </path/to/CSV/file>");
            System.exit(-1);
        }

        try {
            // Init suduko
            Sudoku sudoku = new Sudoku(filename);

            // validate the file
            if (sudoku.validateSolution()) {
                System.out.println("valid");
            } else {
                System.out.println("invalid");
            }
        } catch (IOException ioe) {
            System.err.println("Error reading CSV file: " + filename.getAbsolutePath());
            System.exit(-2);
        } catch (NumberFormatException nfe) {
            System.err.println("CSV file must contain only numbers between 1 and " + AXIS_SIZE);
            System.exit(-3);
        } catch (RuntimeException re) {
            System.err.println(re.getMessage());
            System.exit(-3);
        }
    }

    /**
     * Build a list of pairs using in construction a block. The left value is the row index, the right is the column index
     */
    private void buildBoxIndices() {
        this.blockMap = new ArrayList<>();
        for (int i = 0; i < AXIS_SIZE; i++) {
            for (int j = 0; j < AXIS_SIZE; j++) {
                // calculate the row/column pair for each 3x3 grid. The left generates rows [(0, 1, 2), (3, 4, 5), (6, 7, 8)] for all 9 columns.
                // The right match up the columns [(0, 1, 2), (3, 4, 5), (6, 7, 8)] with each of the 9 rows. The first 9 items in the list
                // correspond to the first 3x3 block, the second 9 to the second 3x3 and so on.
                blockMap.add(new Pair<>((i / BLOCK_SIZE) * BLOCK_SIZE + j / BLOCK_SIZE, i * BLOCK_SIZE % AXIS_SIZE + j % BLOCK_SIZE));
            }
        }

    }

    /**
     * Verify the array contains 9 distinct numbers (1...9). Since the input was validated prior to processing, we know that
     * All input is between 1 and 9. If there are less than 9, the sample is not valid
     *
     * @param sample contains the 9 digits to verify
     * @return true if valid, false for any invalid sample
     */
    private boolean testSample(int[] sample) {
        return Arrays.stream(sample).distinct() // filters duplicates
                .count() == AXIS_SIZE; // checks for 9 unique numbers
    }

    /**
     * Process each row, column, and block
     *
     * @return true if valid, false for any invalid file
     */
    private boolean validateSolution() {
        if (!validateRows()) {
            return false;
        }

        if (!validateColumn()) {
            return false;
        }

        return validateBlock();
    }

    /**
     * Validates each 3x3 block
     *
     * @return true if valid, false for invalid
     */
    private boolean validateBlock() {
        // for each block
        for (int blkNum = 0; blkNum < AXIS_SIZE; blkNum++) {
            // get 9 row/column pairs corresponding to the current block
            List<Pair<Integer, Integer>> pairs = blockMap.subList(blkNum * AXIS_SIZE, blkNum * AXIS_SIZE + AXIS_SIZE);

            int[] block = pairs.stream() // go through each pair
                    .map(p -> test.get(p.getLeft())[p.getRight()]) // get row and column using pair values
                    .mapToInt(Integer::intValue) // get the primitive value each Integer
                    .toArray(); // convert them to an array

            // test the block
            if (!testSample(block)) {
                return false; // no good
            }
        }

        return true; // good
    }

    /**
     * Validate each column
     *
     * @return true if valid, false for invalid
     */
    private boolean validateColumn() {
        // for each column
        for (int colIdx = 0; colIdx < AXIS_SIZE; colIdx++) {
            // effectively final int index necessary for stream
            int finalColIdx = colIdx;

            // get the nth item of every row
            int[] column = test.stream().mapToInt(a -> a[finalColIdx]).toArray();

            // test the column
            if (!testSample(column)) {
                return false; // no good
            }
        }

        return true; // good
    }

    /**
     * Validate each row
     *
     * @return true if valid, false for invalid
     */
    private boolean validateRows() {
        // for each row
        for (int[] row : test) {

            // test the row
            if (!testSample(row)) {
                return false; // no good
            }
        }

        return true; // good
    }

    /**
     * Validate the input of the CSV
     *
     * @throws IllegalArgumentException Throws when something is wrong with the file, message contains the erorr
     */
    private void validateInput() throws IllegalArgumentException {
        // check row length
        if (test.size() != AXIS_SIZE) {
            throw new IllegalArgumentException("There are not enough rows in the CSV file");
        }

        for (int[] row : test) {
            // check column length
            if (row.length != AXIS_SIZE) {
                throw new IllegalArgumentException("There are not enough columns in the CSV file");
            }

            // check if input is between 1 and 9
            for (int i : row) {
                if (i < 1 || i > AXIS_SIZE) {
                    throw new IllegalArgumentException("CSV file must contain numbers between 1 and " + AXIS_SIZE);
                }
            }
        }
    }
}
