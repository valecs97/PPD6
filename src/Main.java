import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) {
        int N=10000;
        //sequencialKaratsuba(N);
        //paralelizedKaratsuba(N);
        poliMulti(N,N);
        paralelizedPoliMulti(N,N);
    }

    private static void poliMulti(int n,int m){
        long start, stop;
        int[] a = fill(n);
        int[] b = fill(m);
        start = System.currentTimeMillis();
        PoliMult.multiply(a,b,n,m);
        stop = System.currentTimeMillis();
        System.out.println((stop - start)/10 + " ms");
    }

    private static void paralelizedPoliMulti(int n,int m){
        long start, stop;
        int[] a = fill(n);
        int[] b = fill(m);
        start = System.currentTimeMillis();
        try {
            PoliMult.multiplyParalel(a,b,n,m);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        stop = System.currentTimeMillis();
        System.out.println((stop - start)/10 + " ms");
    }

    private static void paralelizedKaratsuba(int N){
        long start, stop;
        Random random = new Random();
        BigInteger a = new BigInteger(N, random);
        BigInteger b = new BigInteger(N, random);

        start = System.currentTimeMillis();
        try {
            Karatsuba.karatsubaParalel(a, b);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        //stop = System.currentTimeMillis();
        //System.out.println(stop - start + " ms");

        //start = System.currentTimeMillis();
        //BigInteger d = a.multiply(b);
        stop = System.currentTimeMillis();
        System.out.println((stop - start)/10 + " ms");
        //Async.executor.shutdown();

        //System.out.println((c.equals(d)));
    }

    private static void sequencialKaratsuba(int N){
        long start, stop;
        Random random = new Random();
        BigInteger a = new BigInteger(N, random);
        BigInteger b = new BigInteger(N, random);

        start = System.currentTimeMillis();
        Karatsuba.karatsuba(a, b);
        //stop = System.currentTimeMillis();
        //System.out.println(stop - start + " ms");

        //start = System.currentTimeMillis();
        //BigInteger d = a.multiply(b);
        stop = System.currentTimeMillis();
        System.out.println((stop - start)/10 + " ms");

        //System.out.println((c.equals(d)));
    }

    private static int[] fill(int n){
        int[] a = new int[n];
        Random random = new Random();
        for (int i=0;i<n;i++)
            a[i] = random.nextInt(100000);
        return a;
    }
}
