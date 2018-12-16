import mpi.MPI;
import mpi.Status;

import java.math.BigInteger;
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

    public static int[] multiplyMPI(final int A[], final int B[], int m, int n) {
        int[] prod = new int[m + n - 1];

        // Initialize the porduct polynomial
        for (int i = 0; i < m + n - 1; i++)
            prod[i] = 0;
        for (int i = 0; i < m; i++) {
            int nnn = 0;
            for (int j = 0; j < n; j++) {
                //System.out.println("Preparing sending objects !");
                MPI.COMM_WORLD.Send(new int[]{A[i],B[i]},0,2,MPI.INT,nnn+1,1);
                nnn++;
                //System.out.println("Object send !");
                if (nnn==10 || j==n-1){
                    //System.out.println("FLUSHING !");
                    for (int jj=1;jj<=nnn;jj++){
                        int[] res = new int[1];
                        //System.out.println("Receiving from " + (jj+1));
                        MPI.COMM_WORLD.Recv(res,0,1,MPI.INT,jj,MPI.ANY_TAG);
                        //System.out.println("RESULT RECEIVED !");
                        prod[i+jj] = res[0];
                    }
                    nnn=0;
                }

            }

        }
        return prod;
    }

    public static void multiplyMPIWorker(int me){
        while (true) {
            //System.out.println("Preparing receiving !");
            int[] res = new int[2];
            Status status = MPI.COMM_WORLD.Recv(res, 0, 2, MPI.INT, MPI.ANY_SOURCE, MPI.ANY_TAG);
            //System.out.println("RECEIVED ! " + res + " with tag " + status.tag + " with source " + status.source);

            int ress = res[0] * res[1];

            //System.out.println("NOW SENDING BACK THE RESULT ! " + status.tag);
            MPI.COMM_WORLD.Ssend(new int[]{ress}, 0, 1, MPI.INT, status.source, status.tag);
            //System.out.println("SEND ! " + status.tag);
        }
    }
}
