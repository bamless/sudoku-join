# Fork-Join Sudoku Solver

#### A backtracking sequential and parallel implementation of a sudoku solving algorithm written in java using the forkjoin framework. This is an assignment for the 'Programmazione di Sistemi Multicore' course of the computer science major of 'La Sapienza' university of Rome.
#### Authors: Fabrizio Pietrucci, Davide Pucci.

How to compile:
  1. Navigate to the root folder of the project.
  2. Run form terminal **'ant jar'**. If you don't have **apache ant** download it from here: http://ant.apache.org/bindownload.cgi.

How to use:
  1. Navigate to the **jar** folder generated after compiling
  2. Run from terminal **'java -jar sudoku.jar path'** where:
      - **sudoku.jar** is the jar file in the **jar** folder.
      - **path** is a path to a file containing a sudoku 9x9 board where:
        * Only numbers from 1 to 9 appear. The blank spaces are represented as dots.
        * For examples see the .txt files in the project's root
