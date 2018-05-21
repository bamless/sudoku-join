package com.bizio.sudoku;

/**
 * Exceptions to be thrown when the sudoku passed as an input doesn't respect
 * the size and/or the conventions.
 *
 * @author fabrizio
 *
 */
public class SudokuFormatException extends RuntimeException {
	private static final long serialVersionUID = -3877260841404476466L;

	public SudokuFormatException(String message) {
		super(message);
	}

	public SudokuFormatException(String message, Throwable cause) {
		super(message, cause);
	}

}
