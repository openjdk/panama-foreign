package vector.benchmarks.sgemm;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;


@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 20, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 20, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class SGemmTest {

    @Param({"2.0"})
    float alpha;

    @Param({"0.0"})
    float beta;

    @Param({"8", "16", "32", "64", "128", "256", "512", "1024", "2048", "5096", "10192"})
    int size;

    float[] A;
    float[] B;
    float[] C;

    int length;

    @Setup
    public void setUp() {
        length = size * size;
    }


    @Benchmark
    public float[] arrays() {
        A = new float[length];
        B = new float[length];
        C = new float[length];

        init(A, B);
        return Arrays.sgemm(alpha, A, size, size, size, B, beta, C);
    }

    public static void init(float[] a, float[] b) {
        for (int i = 0; i < a.length; i++) {
            a[i] = 1.0f;
            b[i] = 2.0f;
        }
    }

    static class Arrays {
        static float[] sgemm(float alpha, //scale of (A x B)
                             float[] A, //The A-matrix (row major)
                             int m, //Rows of A, Rows of C
                             int n, //Cols of A, Rows of B
                             int p, //Cols of B, Cols of C
                             float[] B, //The B Matrix (row major)
                             float beta, //The scale of the C matrix
                             float[] C //The C matrix (row major)
        ) {

            float[] b_cmajor = columnMajor(B, n, p);

            for (int i = 0; i < m; i++) {
                for (int j = 0; j < p; j++) {
                    float sum = 0f;
                    for (int k = 0; k < n; k++) {
                        sum += (A[i * n + k] * alpha) * b_cmajor[j * n + k];
                    }
                    C[j * m + i] = C[j * m + i] * beta + sum;
                }
            }
            return C;

        }

        /**
         * Transform a matrix into column-major order from row-major order.
         * Allocates a new matrix.
         *
         * @param matrix The row-major matrix to transform.
         * @param rows The number of rows in this matrix.
         * @param columns The number of columns in this matrix.
         * @return The reference to the new, column-major matrix.
         */
        static float[] columnMajor(float[] matrix, int rows, int columns) {
            float[] colMaj = new float[matrix.length];

            for (int i = 0; i < columns; i++) { //Row #
                int coffset = i * rows;
                for (int j = 0; j < rows; j++) { //Col #
                    colMaj[coffset + j] = matrix[j * columns + i];
                }
            }
            return colMaj;
        }
    }
}