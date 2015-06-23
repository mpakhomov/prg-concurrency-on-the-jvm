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
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.Collections;

public class ConcurrentTotalFileSize {
    class SubDirectoriesAndSize {
        // MP: I added filePath filed for information purpose. I want to know
        // what directory this class contains info for
        final private String filePath;
        final public long size;
        final public List<File> subDirectories;

        public SubDirectoriesAndSize(final String filePath, final long totalSize, final List<File> theSubDirs) {
            this.filePath = filePath;
            size = totalSize;
            subDirectories = Collections.unmodifiableList(theSubDirs);
        }

        @Override
        public String toString() {
            return filePath;
        }
    }

    private SubDirectoriesAndSize getTotalAndSubDirs(final File file) {
        long total = 0;
        final List<File> subDirectories = new ArrayList<File>();
        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            if (children != null)
                for (final File child : children) {
                    if (child.isFile()) {
                        total += child.length();
                    } else {
                        subDirectories.add(child);
                    }
                }
        }
        return new SubDirectoriesAndSize(file.toString(), total, subDirectories);
    }

    private long getTotalSizeOfFilesInDir(final File file)
            throws InterruptedException, ExecutionException, TimeoutException {
        final ExecutorService service = Executors.newFixedThreadPool(100);
        List<SubDirectoriesAndSize> processedFiles = new ArrayList<>();
        try {
            long total = 0;
            final List<File> directories = new ArrayList<File>();
            directories.add(file);
            int depth = 0;
            while (!directories.isEmpty()) {
                depth++;
                final List<Future<SubDirectoriesAndSize>> partialResults =
                        new ArrayList<Future<SubDirectoriesAndSize>>();
                for (final File directory : directories) {
                    partialResults.add(
                            service.submit(new Callable<SubDirectoriesAndSize>() {
                                public SubDirectoriesAndSize call() {
                                    return getTotalAndSubDirs(directory);
                                }
                            }));
                }
                directories.clear();
                processedFiles.clear();
                for (final Future<SubDirectoriesAndSize> partialResultFuture :
                        partialResults) {
                    final SubDirectoriesAndSize subDirectoriesAndSize =
                            partialResultFuture.get(100, TimeUnit.SECONDS);
                    directories.addAll(subDirectoriesAndSize.subDirectories);
                    processedFiles.add(subDirectoriesAndSize);
//                    System.out.println(subDirectoriesAndSize.subDirectories);
                    total += subDirectoriesAndSize.size;
                }
            }
            System.out.println("Maximum depth = " + depth);
            System.out.println(processedFiles);
            return total;
        } finally {
            service.shutdown();
        }
    }

    public static void main(final String[] args)
            throws InterruptedException, ExecutionException, TimeoutException {
        final long start = System.nanoTime();
        final long total = new ConcurrentTotalFileSize()
                .getTotalSizeOfFilesInDir(new File(args[0]));
        final long end = System.nanoTime();
        System.out.println("Total Size: " + total);
        System.out.println("Time taken: " + (end - start) / 1.0e9);
    }
}
