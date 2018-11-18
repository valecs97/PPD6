import java.math.BigInteger;
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
}
