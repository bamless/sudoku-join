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
