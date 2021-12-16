# Sudoku Solution Validator
___
## The purpose of this is to validate a CSV file containing a 9x9 grid of number and verify if they are a solution for a Sudoku puzzle.

No special libraries were used.

###To build, run for the root of project
`$ ./gradlew build jar`

###To run the project
`$ java -jar build/libs/sudoku-1.0-SNAPSHOT.jar samples/example_valid.csv`

or 

`$ java -jar build/libs/sudoku-1.0-SNAPSHOT.jar samples/example_invalid.csv`
