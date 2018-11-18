import java.util.Arrays;
import java.util.concurrent.*;

class AsyncPoli implements Callable<Integer> {
    int a,b,pos;
    int[] prod;

    public AsyncPoli(int a, int b, int pos, int[] prod) {
        this.a = a;
        this.b = b;
        this.pos = pos;
        this.prod = prod;
    }

    @Override
    public Integer call() {
        prod[pos] += a*b;
        return null;
    }
}

public class PoliMult {

    public static int[] multiply(final int A[], final int B[], int m, int n) {
        int[] prod = new int[m + n - 1];

        // Initialize the porduct polynomial
        for (int i = 0; i < m + n - 1; i++)
            prod[i] = 0;

        // Multiply two polynomials term by term

        // Take ever term of first polynomial
        for (int i = 0; i < m; i++) {
            // Multiply the current term of first polynomial
            // with every term of second polynomial.
            for (int j = 0; j < n; j++)
                prod[i + j] += A[i] * B[j];
        }

        return prod;
    }

    public static int[] multiplyParalel(int A[], int B[], int m, int n) throws ExecutionException, InterruptedException {
        int[] prod = new int[m + n - 1];

        // Initialize the porduct polynomial
        for (int i = 0; i < m + n - 1; i++)
            prod[i] = 0;
        for (int i = 0; i < m; i++) {
            FutureTask[] futureTasks = new FutureTask[n];
            for (int j = 0; j < n; j++) {
                AsyncPoli async = new AsyncPoli(A[i],B[i],i+j,prod);
                FutureTask<Integer> futureTask = new FutureTask<>(async);
                futureTasks[j] = futureTask;
            }
            for (int j=0;j<n;j++){
                futureTasks[j].run();
            }
            for (int j=0;j<n;j++){
                futureTasks[j].get();
            }
        }
        return prod;
    }
}
