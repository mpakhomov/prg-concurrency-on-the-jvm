package com.agiledeveloper.pcj.typed;

import akka.actor.TypedActor;

import java.util.concurrent.TimeUnit;

/**
 * @author mpakhomov
 * @since: 7/3/2015
 */
public class EnergySourceImpl implements EnergySource {

    private final long MAXLEVEL = 100L;
    private long level = MAXLEVEL;
    private long usageCount = 0L;


    @Override
    public long getUnitsAvailable() {
        String self = TypedActor.context().self().toString();
        System.out.println("actor: " + self +
                " thread: " + Thread.currentThread().getName());
        return level;
    }

    @Override
    public long getUsageCount() {
        // emulate intensive calculations
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ignore) {}
        String self = TypedActor.context().self().toString();
        System.out.println("actor: " + self +
                " thread: " + Thread.currentThread().getName());
        return usageCount;

    }

    @Override
    public void useEnergy(final long units) {
        String self = TypedActor.context().self().toString();
        if (units > 0 && level - units >= 0) {
            System.out.println("actor: " + self +
                    " thread: " + Thread.currentThread().getName());
            level -= units;
            usageCount++;
        }
        System.out.println("actor: " + self +
                " thread: " + Thread.currentThread().getName() + " return from useEnergy");
    }
}
