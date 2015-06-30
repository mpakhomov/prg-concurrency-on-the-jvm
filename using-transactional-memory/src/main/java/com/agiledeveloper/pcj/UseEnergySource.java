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
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class UseEnergySource {
//    private static final EnergySource energySource = EnergySource.create();

    public static void main(final String[] args)
            throws InterruptedException, ExecutionException {


        final EnergySource energySource = EnergySource.create();

        System.out.println("Energy level at start: " +
                energySource.getUnitsAvailable());

        List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();

        for (int i = 0; i < 10; i++) {
            tasks.add(new Callable<Object>() {
                @Override
                public Object call() {
                    final Random rnd = new Random();
                    for (int j = 0; j < 7; j++) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(rnd.nextInt(2000));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Thread.currentThread().interrupt();
                        }
                        energySource.useEnergy(1);
                    };
                    return null;
                }
            });
        }

        final ExecutorService service = Executors.newFixedThreadPool(10);
        service.invokeAll(tasks);

        System.out.println("Energy level at end: " +
                energySource.getUnitsAvailable());
        System.out.println("Usage: " + energySource.getUsageCount());

        energySource.stopEnergySource();
        service.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!service.awaitTermination(30, TimeUnit.SECONDS)) {
                System.out.println("Running shutdownNow");
                service.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!service.awaitTermination(30, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            service.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
