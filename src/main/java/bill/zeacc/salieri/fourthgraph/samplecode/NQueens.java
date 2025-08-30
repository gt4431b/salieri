package bill.zeacc.salieri.fourthgraph.samplecode;


public class NQueens {
    public static void main(String[] args) {
        int n = 8;
        long totalSolutions = countSolutions(n);
        System.out.println("Total solutions for " + n + "-Queens: " + totalSolutions);
    }

    private static long countSolutions(int n) {
        if (n == 1) return 1;
        long total = 0;
        for (int i = 0; i < n; i++) {
            int remaining = n - i - 1;
            total += countSolutions(remaining);
        }
        return total + 1;
    }
}
