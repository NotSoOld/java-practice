package ru.notsoold.codewars;

import java.util.stream.IntStream;

// https://www.codewars.com/kata/52bb6539a4cf1b12d90005b7
public class BattleshipFieldValidator {

    private static int[][] field;
    private static boolean[][] visited;
    private static int[] ships;

    public static boolean fieldValidator(int[][] f) {
        field = f;
        visited = new boolean[10][10];
        ships = new int[] { 0, 4, 3, 2, 1 };

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (field[i][j] == 1 && !visited[i][j]) {
                    System.out.printf("Visiting %d:%d /// ", i, j);
                    if (continuesDown(i, j)) {
                        // This is a vertical ship.
                        System.out.print("found vertical ship /// ");
                        int i1 = i;
                        int shipLength = 2;
                        //  -  X  !o
                        // !o  -  -
                        markVisited(i1, j);
                        if (!ensureNoNeighboursAndMarkVisited(i1, j + 1)) { return false; }
                        if (!ensureNoNeighboursAndMarkVisited(i1 + 1, j - 1)) { return false; }
                        i1++;
                        while (continuesDown(i1, j)) {
                            shipLength++;
                            // X !o
                            markVisited(i1, j);
                            if (!ensureNoNeighboursAndMarkVisited(i1, j + 1)) { return false; }
                            i1++;
                        }
                        System.out.printf("ship length %d %n", shipLength);
                        if (shipLength > 4 || ships[shipLength] == 0) {
                            // Too many such ships found, or the ship is invalid.
                            return false;
                        }
                        ships[shipLength]--;
                        //  -   X  !o
                        //  !o  o  !o
                        markVisited(i1, j);
                        markVisited(i1 + 1, j);
                        if (!ensureNoNeighboursAndMarkVisited(i1, j + 1)) { return false; }
                        if (!ensureNoNeighboursAndMarkVisited(i1 + 1, j + 1)) { return false; }
                        if (!ensureNoNeighboursAndMarkVisited(i1 + 1, j - 1)) { return false; }

                    } else if (continuesLeft(i, j)) {
                        // This is a horizontal ship.
                        System.out.print("found horizontal ship /// ");
                        int j1 = j;
                        int shipLength = 2;
                        // -   X
                        // !o !o
                        markVisited(i, j1);
                        if (!ensureNoNeighboursAndMarkVisited(i + 1, j1)) { return false; }
                        if (!ensureNoNeighboursAndMarkVisited(i + 1, j1 - 1)) { return false; }
                        j1++;
                        while (continuesLeft(i, j1)) {
                            shipLength++;
                            // X
                            // !o
                            markVisited(i, j1);
                            if (!ensureNoNeighboursAndMarkVisited(i + 1, j1)) { return false; }
                            j1++;
                        }
                        System.out.printf("ship length %d %n", shipLength);
                        if (shipLength > 4 || ships[shipLength] == 0) {
                            // Too many such ships found, or the ship is invalid.
                            return false;
                        }
                        ships[shipLength]--;
                        //  -  X  o
                        // !o !o !o
                        markVisited(i, j1);
                        markVisited(i, j1 + 1);
                        if (!ensureNoNeighboursAndMarkVisited(i + 1, j1)) { return false; }
                        if (!ensureNoNeighboursAndMarkVisited(i + 1, j1 + 1)) { return false; }
                        if (!ensureNoNeighboursAndMarkVisited(i + 1, j1 - 1)) { return false; }

                    } else {
                        // This is single cell ship.
                        if (ships[1] == 0) {
                            // Too many single cell ships found.
                            return false;
                        }
                        ships[1]--;
                        //  -  X  o
                        // !o  o !o
                        markVisited(i, j);
                        markVisited(i + 1, j);
                        markVisited(i, j + 1);
                        if(!ensureNoNeighboursAndMarkVisited(i + 1, j + 1)) { return false; }
                        if(!ensureNoNeighboursAndMarkVisited(i + 1, j - 1)) { return false; }
                        System.out.println("found single ship");
                    }
                }
            }
        }

        return IntStream.of(ships).sum() == 0;
    }


    private static boolean ensureNoNeighboursAndMarkVisited(int i, int j) {
        if (!isValidCell(i, j)) {
            return true;
        }
        if (field[i][j] == 1) {
            // We expect it to be an empty cell.
            System.out.printf("%nUnexpected neighbour at %d:%d%n", i, j);
            return false;
        }
        markVisited(i, j);
        return true;
    }

    private static void markVisited(int i, int j) {
        if (isValidCell(i, j)) {
            visited[i][j] = true;
        }
    }

    private static boolean continuesDown(int i, int j) {
        if (isValidCell(i + 1, j)) {
            if (field[i + 1][j] == 1) {
                return true;
            }
        }
        return false;
    }

    private static boolean continuesLeft(int i, int j) {
        if (isValidCell(i, j + 1)) {
            if (field[i][j + 1] == 1) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidCell(int i, int j) {
        return i >= 0 && i < 10 && j >= 0 && j < 10;
    }


    public static void main(String[] args) {
        int[][] battleField =  {{1, 0, 0, 0, 0, 1, 1, 0, 0, 0},
                                {1, 0, 1, 0, 0, 0, 0, 0, 1, 0},
                                {1, 0, 1, 0, 1, 1, 1, 0, 1, 0},
                                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                                {0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
                                {0, 0, 0, 1, 0, 0, 0, 0, 1, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

        System.out.println(fieldValidator(battleField));
        IntStream.of(ships).forEach(System.out::print);


    }
}
