/*
 * Copyright (C) 2017  Fabrizio Pietrucci, Davide Pucci
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bizio.sudoku;

import java.io.File;
import java.io.FileNotFoundException;

public class Main {

	public static void main(String[] args) {
		runScript(args);
	}

	/**
	 * Runs a script that prints the solution space size, ratio of fixed cells,
	 * execution time for solving and the number of solutions of the input
	 * sudoku board. It doesn't print the stacktrace of sudokuformat and
	 * filenotfound exceptions, but logs them in a user readable form.
	 *
	 * @param args
	 *            the script arguments, in this case only the path of the file
	 *            containing the sudoku.
	 */
	private static void runScript(String args[]) {
		if (args.length < 1 || args.length > 1)
			usage();

		SudokuBoard board = null;
		try {
			board = new SudokuBoard(new File(args[args.length - 1]));
		} catch (FileNotFoundException | SudokuFormatException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		benchmark(board);
	}

	private static void benchmark(SudokuBoard board) {
		String searchSpace =  board.getSearchSpaceScientific();
		int fillFactor = board.getFillFactor();

		System.out.printf("Empty cells: %d%n", SudokuBoard.N * SudokuBoard.N - board.getFixedCells());
		System.out.printf("Fill ratio: %d%%%n", fillFactor);
		System.out.printf("Search space: %s%n%n", searchSpace);

		long begin;
		long end;

		System.out.println("Solving sequentially...");

		begin = System.currentTimeMillis();
		int seqSol = board.solve();
		end = System.currentTimeMillis();

		long seqTime = end - begin;

		System.out.printf("Done in: %s%n", formatMilliseconds(seqTime));
		System.out.printf("Solutions: %d%n%n", seqSol);

		System.out.println("Solving in parallel...");

		begin = System.currentTimeMillis();
		int parSol = board.parallelSolve();
		end = System.currentTimeMillis();

		long parTime = end - begin;

		System.out.printf("Done in: %s%n", formatMilliseconds(parTime));
		System.out.printf("Solutions: %d%n%n", parSol);

		double speedUp = (double) seqTime/parTime;
		System.out.printf("Speedup: %f%n", speedUp);
	}

	private static String formatMilliseconds(long millis) {
		String s = "";
		long min = (millis / 1000) / 60;
		long sec = (millis / 1000) % 60;
		long ms = millis % 1000;

		s += min + "m ";
		s += sec + "s ";
		s += ms + "ms";
		return s;
	}

	private static void usage() {
		System.err.println("Usage: java -jar sudoku.jar path");
		System.exit(1);
	}

}
