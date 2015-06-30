/***
 * Excerpted from "Programming Concurrency on the JVM",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/vspcon for more book information.
 ***/
package com.agiledeveloper.pcj;

//import akka.stm.Ref;
//import akka.stm.Atomic;

import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnBooleanCallable;
import org.multiverse.api.callables.TxnIntCallable;
import org.multiverse.api.callables.TxnLongCallable;
import org.multiverse.api.references.*;
//import static org.multiverse.api.StmUtils.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EnergySource {
    private final long MAXLEVEL = 100;
    private final TxnLong level = StmUtils.newTxnLong(MAXLEVEL);
    private final TxnLong usageCount = StmUtils.newTxnLong(0L);
    private final TxnBoolean keepRunning = StmUtils.newTxnBoolean(true);
    private static final ScheduledExecutorService replenishTimer =
            Executors.newScheduledThreadPool(10);


    private EnergySource() {
    }

    private void init() {
        replenishTimer.schedule(new Runnable() {
            public void run() {
                replenish();
                System.out.println("keepRunning=" + keepRunning.atomicGet());
                if (keepRunning.atomicGet()) replenishTimer.schedule(
                        this, 1, TimeUnit.SECONDS);
            }
        }, 1, TimeUnit.SECONDS);
    }

    public static EnergySource create() {
        final EnergySource energySource = new EnergySource();
        energySource.init();
        return energySource;
    }

    public void stopEnergySource() {
        StmUtils.atomic(() -> keepRunning.set(false));
        System.out.println("Running replenishTimer.shutdown()");
        replenishTimer.shutdown();
        try {
            System.out.println("Running replenishTimer.shutdownNow()");
            if (!replenishTimer.awaitTermination(30, TimeUnit.SECONDS)) {
                replenishTimer.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            replenishTimer.shutdownNow();
        }
    }

    public long getUnitsAvailable() {
        return level.atomicGet();
//        return StmUtils.atomic(
//                (Txn txn) -> level.get()
//        );

    }

    public long getUsageCount() {
        return usageCount.atomicGet();
//        return StmUtils.atomic(
//            (Txn txn) -> usageCount.get()
//        );
    }

    public boolean useEnergy(final long units) {
        return StmUtils.atomic(new TxnBooleanCallable() {
            @Override
            public boolean call(Txn txn) throws Exception {
                long currentLevel = level.get();
                if (units > 0 && currentLevel >= units) {
                    level.set(currentLevel - units);
                    usageCount.increment();
                    return true;
                } else {
                    return false;
                }
            }
        });
//    return  new Atomic<Boolean>() {
//      public Boolean atomically() {
//        long currentLevel = level.get();
//        if(units > 0 && currentLevel >= units) {
//          level.swap(currentLevel - units);
//          usageCount.swap(usageCount.get() + 1);
//          return true;
//        } else {
//          return false;
//        }
//      }
//    }.execute();
    }

    private void replenish() {
        System.out.println("Running replenish");
        StmUtils.atomic(() -> {
            long currentLevel = level.get();
            System.out.println("currentLevel="+currentLevel);
            if (currentLevel < MAXLEVEL) {
                level.increment();
//                level.set(currentLevel + 1);
            }
            System.out.println("Replenish done");
            return;
        });
//    new Atomic() {
//      public Object atomically() {
//        long currentLevel = level.get();
//        if (currentLevel < MAXLEVEL) level.swap(currentLevel + 1);
//        return null;
//      }
//    }.execute();
    }
}
