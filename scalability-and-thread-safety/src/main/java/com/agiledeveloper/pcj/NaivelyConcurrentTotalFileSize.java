/***
 * Excerpted from "Programming Concurrency on the JVM",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/vspcon for more book information.
 ***/
package com.agiledeveloper.pcj;

import java.io.File;
import java.util.ArrayDeque;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

public class NaivelyConcurrentTotalFileSize {
    private long getTotalSizeOfFilesInDir(
            final ExecutorService service, final File file)
            throws InterruptedException, ExecutionException, TimeoutException {
        System.out.println("Thread "  + Thread.currentThread() + " getTotalSizeOfFilesInDir: file " + file);
        if (file.isFile()) return file.length();

        long total = 0;
        final File[] children = file.listFiles();
        long begin, end;

        if (children != null) {
            final List<Future<Long>> partialTotalFutures =
                    new ArrayList<Future<Long>>();
//            final List<String> childs = new ArrayList<>();
            final ArrayDeque<String> childs = new ArrayDeque<>(Integer.MAX_VALUE / 1024);
            for (final File child : children) {
                begin = System.currentTimeMillis();
                System.out.println("Thread "  + Thread.currentThread() + " getTotalSizeOfFilesInDir. recursion into child: " + child);
                partialTotalFutures.add(service.submit(new Callable<Long>() {
                    public Long call() throws InterruptedException,
                            ExecutionException, TimeoutException {
                        return getTotalSizeOfFilesInDir(service, child);
                    }
                }));
                childs.add(child.toString());
                end = System.currentTimeMillis();
                System.out.println("Thread "  + Thread.currentThread() + " service.submit ran " + (end - begin) + " ms");
            }

//            int i = 0;
            for (final Future<Long> partialTotalFuture : partialTotalFutures) {
                begin = System.currentTimeMillis();
                System.out.println("Thread "  + Thread.currentThread() + " waiting for completion of " + childs.poll());
                total += partialTotalFuture.get();
//                total += partialTotalFuture.get(100, TimeUnit.SECONDS);
                end = System.currentTimeMillis();
                System.out.println("Thread "  + Thread.currentThread() + " future.get ran " + (end - begin) + " ms");
            }
        }

        return total;
    }

    private long getTotalSizeOfFile(final String fileName)
            throws InterruptedException, ExecutionException, TimeoutException {
        final int poolSize = 4;
        System.out.println("Folder: " + fileName);
        System.out.println("Pool Size: " + poolSize);
        final ExecutorService service = Executors.newFixedThreadPool(poolSize);
        try {
            return getTotalSizeOfFilesInDir(service, new File(fileName));
        } finally {
            service.shutdown();
        }
    }

    public static void main(final String[] args)
            throws InterruptedException, ExecutionException, TimeoutException {
        final long start = System.nanoTime();
        final long total = new NaivelyConcurrentTotalFileSize()
                .getTotalSizeOfFile(args[0]);
        final long end = System.nanoTime();
        System.out.println("Total Size: " + total);
        System.out.println("Time taken: " + (end - start) / 1.0e9);
    }
}
