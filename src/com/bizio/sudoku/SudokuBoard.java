package com.bizio.sudoku;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Class that represent a 9x9 sudoku board. Implements functions for solving the
 * sudoku using a backtracking algorithm that returns the number of all the
 * possible solutions.
 *
 * @author fabrizio
 *
 */
public class SudokuBoard {
	/** Size of the classic sudoku board */
	public final static int N = 9;
	/** Sequential cutoff for the fork-join parallel solve */
	private final static BigInteger CUTOFF_SEARCHSPACE = new BigDecimal("1E25").toBigInteger();
	/** Fork join pool for the parallel solve */
	public static final ForkJoinPool pool = new ForkJoinPool();
	/** Number of fixed cells in this sudoku */
	private int fixed;
	/** The sudoku board */
	private byte board[][];

	public SudokuBoard(byte board[][]) {
		if (board.length != N)
			throw new SudokuFormatException(String.format("The sudoku board must be %dx%d", N, N));
		for (byte[] b : board) {
			if (b.length != N)
				throw new SudokuFormatException(String.format("The sudoku board must be %dx%d", N, N));
		}

		this.board = board;
		computeFixed();
	}

	/**
	 * Builds a {@link SudokuBoard} from a continous string. the points represent
	 * blank spaces.
	 *
	 * @param str the string to parse
	 */
	public SudokuBoard(String str) {
		this.board = parseString(str);
		computeFixed();
	}

	/** Creates a deep copy of another {@link SudokuBoard} */
	public SudokuBoard(SudokuBoard o) {
		this.board = new byte[N][N];
		for (int i = 0; i < N; i++)
			board[i] = Arrays.copyOf(o.board[i], o.board[i].length);
		fixed = o.fixed;
	}

	/** Reads a board from file. See README for board formatting */
	public SudokuBoard(File file) throws FileNotFoundException {
		this.board = readBoardFromFile(file);
		computeFixed();
	}

	/**
	 * Solves the sudoku using a backtracking algorithm that finds all the
	 * possible solutions.
	 *
	 * @return the number of solutions
	 */
	public int solve() {
		return solve(0, 0);
	}

	private int solve(int row, int col) {
		//base case, solution found!
		if (col == N)
			return 1;

		// if the current cell is not empty (is a fixed cell) do not change it
		if (!isEmpty(row, col))
			return row < N - 1 ? solve(row + 1, col) : solve(0, col + 1);

		//the result
		int res = 0;
		// try all the possible numbers. If the number selected is valid increase
		// the cell and proceed with the recursion
		for (byte num = 1; num <= N; num++) {
			if (isValid(row, col, num)) {
				setCell(row, col, num);
				res += row < N - 1 ? solve(row + 1, col) : solve(0, col + 1);
				setCell(row, col, (byte) 0);
			}
		}
		return res;
	}

	/**
	 * Solves the sudoku using a backtracking algorithm executed in parallel
	 * with the fork join framework.
	 *
	 * @return the number of solutions
	 */
	public int parallelSolve() {
		return pool.invoke(new SolverTask(this));
	}

	/**
	 * Checks if a number can be placed in a cell without breaking the rules of
	 * sudoku. Used in the backtracking algorithm in order to reduce the search
	 * space.
	 *
	 * @see #solve()
	 *
	 * @param val the value to be checked
	 * @param row cell row
	 * @param col cell column
	 * @return true if the value can be placed without breaking the rules of sudoku, false otherwise
	 */
	private boolean isValid(int row, int col, byte val) {
		// check for equals numbers on the column and the row
		for (int i = 0; i < N; i++) {
			if (getCell(row, i) == val || getCell(i, col) == val)
				return false;
		}

		// find the section coordinates
		int rowSec = (row / 3) * 3;
		int colSec = (col / 3) * 3;

		// compute the remaining cells to check and check if in one of
		// them there is a number equal to num
		int row1 = (row + 2) % 3;
		int row2 = (row + 4) % 3;
		int col1 = (col + 2) % 3;
		int col2 = (col + 4) % 3;

		if (getCell(row1 + rowSec, col1 + colSec) == val) return false;
		if (getCell(row2 + rowSec, col1 + colSec) == val) return false;
		if (getCell(row1 + rowSec, col2 + colSec) == val) return false;
		if (getCell(row2 + rowSec, col2 + colSec) == val) return false;

		return true;
	}

	/** Parses a string and builds a byte array from it */
	private byte[][] parseString(String str) {
		if (str.length() != N * N)
			throw new SudokuFormatException(String.format("The sudoku board must be %dx%d", N, N));

		byte[][] board = new byte[N][N];
		for (int i = 0; i < str.length(); i++) {
			byte num;
			try {
				num = str.charAt(i) != '.' ? Byte.parseByte(str.charAt(i) + "") : 0;
			} catch (NumberFormatException e) {
				throw new SudokuFormatException("The sudoku board string must contain only numbers "
						+ "from 1 to 9 or points (indicating blank spaces)", e);
			}
			board[i / 9][i % 9] = num;
		}
		return board;
	}

	/**
	 * Reads a sudoku board from file. See README for file formatting.
	 *
	 * @param file The file containing the sudoku board
	 * @return The board as table of bytes
	 * @throws FileNotFoundException if the file isn't found
	 */
	private byte[][] readBoardFromFile(File file) throws FileNotFoundException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null)
				sb.append(line);
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return parseString(sb.toString());
	}

	/** @return the specified cell */
	public byte getCell(int row, int col) {
		return board[row][col];
	}

	/** Sets a val in the cell */
	public void setCell(int row, int col, byte val) {
		if (!isEmpty(row, col) && val == 0)
			fixed--;
		else if (isEmpty(row, col) && val != 0)
			fixed++;
		board[row][col] = val;
	}

	/** @return true if the specified cell is filled (not 0), false otherwise */
	public boolean isEmpty(int row, int col) {
		return getCell(row, col) == 0;
	}

	/** Updates the number of fixed cells in the sudoku */
	private void computeFixed() {
		for (int row = 0; row < N; row++) {
			for (int col = 0; col < N; col++) {
				if (getCell(row, col) != 0)
					fixed++;
			}
		}
	}

	/** @return the approximate solution space as a {@link BigInteger} */
	public BigInteger getSearchSpace() {
		BigInteger solSpace = BigInteger.valueOf(1);
		for (int row = 0; row < N; row++) {
			for (int col = 0; col < N; col++) {
				if (getCell(row, col) == 0) {
					int candidates = 0;
					for (byte num = 1; num <= 9; num++) {
						if (isValid(row, col, num))
							candidates++;
					}
					solSpace = solSpace.multiply(BigInteger.valueOf(candidates));
				}
			}
		}
		return solSpace;
	}

	/**
	 * @return the approximate solution space as a string formatted in
	 * scientific notation
	 */
	public String getSearchSpaceScientific() {
		BigInteger solSpace = getSearchSpace();
		String solSpaceScientific = solSpace.toString();
		if (solSpace.compareTo(BigInteger.valueOf(100000)) > 0) {
			NumberFormat formatter = new DecimalFormat("0.######E0", DecimalFormatSymbols.getInstance(Locale.ROOT));
			solSpaceScientific = formatter.format(solSpace);
		}
		return solSpaceScientific;
	}

	/** @return the number of fixed cells */
	public int getFixedCells() {
		return fixed;
	}

	/** @return the percentage of fixed cell as an int */
	public int getFillFactor() {
		return Math.round((((float) fixed / (N * N)) * 100));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int y = 0; y < 9; y++) {
			for (int x = 0; x < 9; x++)
				sb.append(getCell(x, y) + " ");
			sb.append('\n');
		}
		return sb.toString();
	}

	/**
	 * Fork-join action for solving a sudoku in parallel.
	 *
	 * @author fabrizio
	 */
	private class SolverTask extends RecursiveTask<Integer> {
		private static final long serialVersionUID = 7541995084434430895L;
		/** The sudoku board */
		private SudokuBoard board;
		/** The position in the board of the current task */
		private int row, col;

		public SolverTask(int row, int col, SudokuBoard board) {
			this.board = board;
			this.col = col;
			this.row = row;
		}

		public SolverTask(SudokuBoard board) {
			this(0, 0, board);
		}

		@Override
		protected Integer compute() {
			// if the search space is smaller than the cutoff, solve sequentially
			if (board.getSearchSpace().compareTo(CUTOFF_SEARCHSPACE) < 0)
				return board.solve(row, col);
			else
				return parSolve();
		}

		/** Parallel backtracking algorithm */
		private int parSolve() {
			// base case, solution found!
			if (col == N)
				return 1;

			// if the current cell is not empty (is a fixed cell) do not change it and proceed with the recursion
			if (!board.isEmpty(row, col)) {
				// no need to copy the sudoku, so pass the board field of the sudoku
				SolverTask task = row < N - 1 ? new SolverTask(row + 1, col, new SudokuBoard(board.board))
						: new SolverTask(0, col + 1, new SudokuBoard(board.board));
				// do not fork, compute directly
				return task.compute();
			}

			// try all the possible numbers. If the number selected is valid increase the cell and
			// add a new solver task to the queue for execution
			List<SolverTask> tasks = new ArrayList<>();
			for (byte num = 1; num <= N; num++) {
				if (board.isValid(row, col, num)) {
					board.setCell(row, col, num);
					tasks.add(row < N - 1 ? new SolverTask(row + 1, col, new SudokuBoard(board))
							: new SolverTask(0, col + 1, new SudokuBoard(board)));
					board.setCell(row, col, (byte) 0);
				}
			}
			board = null; // makes the board eligible for garbage collection

			// the result of this thread
			int res = 0;
			// execute in parallel all the actions in the queue. The last action in the queue does not fork but get executed in this thread.
			SolverTask continuation = tasks.get(tasks.size() - 1);
			tasks.remove(tasks.size() - 1);

			for (SolverTask task : tasks)
				task.fork();

			res += continuation.compute();

			// wait for the forked tasks to finish
			for (SolverTask task : tasks)
				res += task.join();
			return res;
		}
	}

}
