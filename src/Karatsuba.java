import mpi.MPI;
import mpi.Status;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.*;
/*
class Async {
    public static ExecutorService executor = Executors.newCachedThreadPool();

    public Future<BigInteger> calculate(BigInteger x, BigInteger y) {
        return executor.submit(() -> Karatsuba.karatsubaParalel(x,y));
    }
}*/

class Async implements Callable<BigInteger> {
    BigInteger x,y;

    public Async(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public BigInteger call() throws Exception {
        return Karatsuba.karatsubaParalel(x,y);
    }
}

class Async2 implements Callable<BigInteger> {
    BigInteger x,y;
    int dest;

    public Async2(BigInteger x, BigInteger y,int dest) {
        this.x = x;
        this.y = y;
        this.dest = dest;
    }

    @Override
    public BigInteger call() throws Exception {
        return Karatsuba.karatsubaMPI(x,y,dest);
    }
}

public class Karatsuba {
    private final static BigInteger ZERO = new BigInteger("0");

    public static BigInteger karatsuba(BigInteger x, BigInteger y) {

        // cutoff to brute force
        int N = Math.max(x.bitLength(), y.bitLength());
        if (N <= 2000) return x.multiply(y);                // optimize this parameter

        // number of bits divided by 2, rounded up
        N = (N / 2) + (N % 2);

        // x = a + 2^N b,   y = c + 2^N d
        BigInteger b = x.shiftRight(N);
        BigInteger a = x.subtract(b.shiftLeft(N));
        BigInteger d = y.shiftRight(N);
        BigInteger c = y.subtract(d.shiftLeft(N));

        // compute sub-expressions
        BigInteger ac = karatsuba(a, c);
        BigInteger bd = karatsuba(b, d);
        BigInteger abcd = karatsuba(a.add(b), c.add(d));

        return ac.add(abcd.subtract(ac).subtract(bd).shiftLeft(N)).add(bd.shiftLeft(2 * N));
    }

    public static BigInteger karatsubaParalel(BigInteger x, BigInteger y) throws ExecutionException, InterruptedException {

        // cutoff to brute force
        int N = Math.max(x.bitLength(), y.bitLength());
        if (N <= 2000) return x.multiply(y);                // optimize this parameter

        // number of bits divided by 2, rounded up
        N = (N / 2) + (N % 2);

        // x = a + 2^N b,   y = c + 2^N d
        BigInteger b = x.shiftRight(N);
        BigInteger a = x.subtract(b.shiftLeft(N));
        BigInteger d = y.shiftRight(N);
        BigInteger c = y.subtract(d.shiftLeft(N));

        // compute sub-expressions
        //Future<BigInteger> acFuture = new Async().calculate(a,c);
        //Future<BigInteger> bdFuture = new Async().calculate(b,d);
        //Future<BigInteger> abcdFuture = new Async().calculate(a.add(b),c.add(d));

        Async async1 = new Async(a,c);
        FutureTask<BigInteger> acFuture = new FutureTask<>(async1);
        acFuture.run();

        Async async2 = new Async(b,d);
        FutureTask<BigInteger> bdFuture = new FutureTask<>(async2);
        bdFuture.run();

        Async async3 = new Async(a.add(b),c.add(d));
        FutureTask<BigInteger> abcdFuture = new FutureTask<>(async3);
        abcdFuture.run();

        BigInteger ac = acFuture.get();
        BigInteger bd = bdFuture.get();
        BigInteger abcd = abcdFuture.get();

        return ac.add(abcd.subtract(ac).subtract(bd).shiftLeft(N)).add(bd.shiftLeft(2 * N));
    }

    public static BigInteger karatsubaMPI(BigInteger x, BigInteger y,int dest) throws ExecutionException, InterruptedException {

        // cutoff to brute force
        int N = Math.max(x.bitLength(), y.bitLength());
        System.out.println("SIZE : " + N);
        if (N <= 2000) return x.multiply(y);                // optimize this parameter

        System.out.println("Preparing sending objects !");
        MPI.COMM_WORLD.Send(new int[]{N},0,1,MPI.INT,dest,1);
        MPI.COMM_WORLD.Send(new BigInteger[]{x,y},0,2,MPI.OBJECT,dest,1);
        System.out.println("First object send !");
        BigInteger[] res = new BigInteger[4];
        MPI.COMM_WORLD.Recv(res,0,4,MPI.OBJECT,dest,MPI.ANY_TAG);
        System.out.println("RESULT RECEIVED !");

        Async2 async1 = new Async2(res[0],res[2],1);
        FutureTask<BigInteger> acFuture = new FutureTask<>(async1);
        acFuture.run();

        Async2 async2 = new Async2(res[1],res[3],2);
        FutureTask<BigInteger> bdFuture = new FutureTask<>(async2);
        bdFuture.run();

        Async2 async3 = new Async2(res[0].add(res[1]),res[2].add(res[3]),3);
        FutureTask<BigInteger> abcdFuture = new FutureTask<>(async3);
        abcdFuture.run();

        BigInteger ac = acFuture.get();
        BigInteger bd = bdFuture.get();
        BigInteger abcd = abcdFuture.get();


        /*System.out.println("Preparing sending objects !");
        MPI.COMM_WORLD.Send(new BigInteger[]{a,c},0,2,MPI.OBJECT,1,1);
        System.out.println("First object send !");
        MPI.COMM_WORLD.Send(new BigInteger[]{b,d},0,2,MPI.OBJECT,1,2);
        System.out.println("Second object send !");
        MPI.COMM_WORLD.Send(new BigInteger[]{a.add(b),c.add(d)},0,2,MPI.OBJECT,1,3);
        System.out.println("Third object send !");

        BigInteger ac = BigInteger.valueOf(0);
        BigInteger bd = BigInteger.valueOf(0);
        BigInteger abcd = BigInteger.valueOf(0);
        BigInteger[] res = new BigInteger[1];
        for (int i=0;i<3;i++){
            System.out.println("Wating for results !");
            Status status = MPI.COMM_WORLD.Recv(res,0,1,MPI.OBJECT,MPI.ANY_SOURCE,MPI.ANY_TAG);
            System.out.println("New calculated ! " + status.tag);
            switch (status.tag){
                case 1:
                    ac = res[0];break;
                case 2:
                    bd = res[0];break;
                case 3:
                    abcd = res[0];break;
            }
        }*/


        return ac.add(abcd.subtract(ac).subtract(bd).shiftLeft(N)).add(bd.shiftLeft(2 * N));
    }

    /*public static void karatsubaWorkerMPI(int me){
        System.out.println("Preparing receiving !");
        BigInteger[] res = new BigInteger[2];
        Status status = MPI.COMM_WORLD.Recv(res,0,2,MPI.OBJECT,MPI.ANY_SOURCE,MPI.ANY_TAG);
        System.out.println("RECEIVED ! " + res + " with tag " + status.tag + " with source " + status.source);
        BigInteger ress = karatsubaMPI(res[0],res[1]);
        System.out.println("NOW SENDING BACK THE RESULT ! " + status.tag);
        MPI.COMM_WORLD.Ssend(new BigInteger[]{ress},0,1,MPI.OBJECT,status.source,status.tag);
        System.out.println("SEND ! " + status.tag);
    }*/

    public static void karatsubaWorkerMPI(int me){
        while (true) {
            System.out.println("Preparing receiving !");
            int[] siz = new int[1];
            Status status = MPI.COMM_WORLD.Recv(siz, 0, 1, MPI.INT, MPI.ANY_SOURCE, MPI.ANY_TAG);
            BigInteger[] res = new BigInteger[2];
            MPI.COMM_WORLD.Recv(res, 0, 3, MPI.OBJECT, status.source, status.tag);
            System.out.println("RECEIVED ! " + res + " with tag " + status.tag + " with source " + status.source);

            int N = siz[0];
            BigInteger x = res[0];
            BigInteger y = res[1];
            // number of bits divided by 2, rounded up
            N = (N / 2) + (N % 2);

            // x = a + 2^N b,   y = c + 2^N d
            BigInteger b = x.shiftRight(N);
            BigInteger a = x.subtract(b.shiftLeft(N));
            BigInteger d = y.shiftRight(N);
            BigInteger c = y.subtract(d.shiftLeft(N));

            System.out.println("NOW SENDING BACK THE RESULT ! " + status.tag);
            MPI.COMM_WORLD.Ssend(new BigInteger[]{a,b,c,d}, 0, 4, MPI.OBJECT, status.source, status.tag);
            System.out.println("SEND ! " + status.tag);
        }
    }
}
