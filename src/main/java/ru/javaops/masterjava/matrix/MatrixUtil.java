package ru.javaops.masterjava.matrix;

import java.util.Random;
import java.util.concurrent.*;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        final int[][] matrixBT = new int[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrixBT[j][i] = matrixB[i][j];
            }
        }

        CountDownLatch latch = new CountDownLatch(matrixSize);
        for (int i = 0; i < matrixSize; i++) {
            final int n = i;
            executor.submit(() -> {
                for (int j = 0; j < matrixSize; j++) {
                    int sum = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        sum += matrixA[n][k] * matrixBT[j][k];
                    }
                    matrixC[n][j] = sum;
                }
                latch.countDown();
            });
        }

        latch.await(10, TimeUnit.SECONDS);

        return matrixC;
    }

    public static int[][] concurrentComplitetServiceMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor)
            throws InterruptedException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        class ColResult {
            int colNum;
            int[] colBody;

            public ColResult(int colNum, int[] colBody) {
                this.colNum = colNum;
                this.colBody = colBody;
            }
        }

        CompletionService<ColResult> completionService = new ExecutorCompletionService<ColResult>(executor);

        for (int i = 0; i < matrixSize; i++) {
            final int col = i;
            final int[] columnBT = new int[matrixSize];

            for (int j = 0; j < matrixSize; j++) {
                columnBT[j] = matrixB[j][i];
            }

            completionService.submit(() -> {
                final int[] columnC = new int[matrixSize];
                for (int j = 0; j < matrixSize; j++) {
                    final int[] rowA = matrixA[j];
                    int sum = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        sum += rowA[k] * columnBT[k];
                    }
                    columnC[j] = sum;
                }
                return new ColResult(col, columnC);
            });
        }

        for (int i = 0; i < matrixSize; i++) {
            try {
                final ColResult result = completionService.poll(10, TimeUnit.SECONDS).get();
                for (int j = 0; j < matrixSize; j++) {
                    matrixC[j][result.colNum] = result.colBody[j];
                }
            } catch (ExecutionException ex) {
                throw new InterruptedException(ex.getMessage());
            }
        }

        return matrixC;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        final int[][] matrixBT = new int[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrixBT[j][i] = matrixB[i][j];
            }
        }

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * matrixBT[j][k];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
