package bill.zeacc.salieri.fourthgraph.samplecode;


public class EightQueens {
    public static void main(String[] args) {
        Queen[] queens = new Queen[8];
        placeQueen(0, queens);
    }

    private static void placeQueen(int row, Queen[] queens) {
        if (row == 8) {
            printSolution(queens);
            return;
        }
        for (int col = 0; col < 8; col++) {
            if (isSafe(row, col, queens)) {
                Queen queen = new Queen(row, col);
                queens[row] = queen;
                placeQueen(row + 1, queens);
                queens[row] = null;
            }
        }
    }

    private static boolean isSafe(int row, int col, Queen[] queens) {
        for (int i = 0; i < row; i++) {
            if (queens[i].col == col || Math.abs(i - row) == Math.abs(queens[i].col - col)) {
                return false;
            }
        }
        return true;
    }

    private static void printSolution(Queen[] queens) {
        for (Queen queen : queens) {
            System.out.println("Queen at (" + queen.row + ", " + queen.col + ")");
        }
    }
}

class Queen {
    int row, col;

    public Queen(int row, int col) {
        this.row = row;
        this.col = col;
    }
}