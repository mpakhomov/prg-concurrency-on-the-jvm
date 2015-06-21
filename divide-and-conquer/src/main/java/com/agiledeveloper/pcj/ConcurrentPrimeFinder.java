/***
 * Excerpted from "Programming Concurrency on the JVM",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/vspcon for more book information.
 ***/
package com.agiledeveloper.pcj;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConcurrentPrimeFinder extends AbstractPrimeFinder {
    private final int poolSize;
    private final int numberOfParts;

    public ConcurrentPrimeFinder(final int thePoolSize,
                                 final int theNumberOfParts) {
        poolSize = thePoolSize;
        numberOfParts = theNumberOfParts;
    }

    public int countPrimes(final int number) {
        System.out.println("Number = " + number);
        System.out.println("Pool size = " + this.poolSize);
        System.out.println("Number of parts = " + this.numberOfParts);
        int count = 0;
        try {
            final List<Callable<Integer>> partitions =
                    new ArrayList<Callable<Integer>>();
            final int chunksPerPartition = number / numberOfParts;
            for (int i = 0; i < numberOfParts; i++) {
                final int lower = (i * chunksPerPartition) + 1;
                final int upper =
                        (i == numberOfParts - 1) ? number
                                : lower + chunksPerPartition - 1;
                partitions.add(new Callable<Integer>() {
                    public Integer call() {
                        return countPrimesInRange(lower, upper);
                    }
                });
            }
            final ExecutorService executorPool =
                    Executors.newFixedThreadPool(poolSize);
            long begin = System.currentTimeMillis();
            final List<Future<Integer>> resultFromParts =
                    executorPool.invokeAll(partitions, 10000, TimeUnit.SECONDS);
            long end = System.currentTimeMillis();
            System.out.println("ExecutorService.invokeAll ran " + (end - begin) + " ms");
            System.out.println("Shutting down the thread pool");
            begin = System.currentTimeMillis();
            executorPool.shutdown();
            end = System.currentTimeMillis();
            System.out.println("The thread pool was shut down");
            System.out.println("it took " + (end - begin) + " ms to shutdown the pool");
            for (final Future<Integer> result : resultFromParts) {
                begin = System.currentTimeMillis();
                count += result.get();
                end = System.currentTimeMillis();
                System.out.println("Future.get ran in " + (end - begin) + " ms");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return count;
    }

    public static void main(final String[] args) {
        if (args.length < 3)
            System.out.println("Usage: number poolsize numberOfParts");
        else
            new ConcurrentPrimeFinder(
                    Integer.parseInt(args[1]), Integer.parseInt(args[2]))
                    .timeAndCompute(Integer.parseInt(args[0]));
    }
}
