package com.agiledeveloper.pcj.typed2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.TypedActor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * @author mpakhomov
 * @since: 7/3/2015
 */
public class EnergySourceImpl implements EnergySource, TypedActor.PreStart, TypedActor.Receiver,
                                         TypedActor.PostStop {

    private final long MAXLEVEL = 100L;
    private long level = MAXLEVEL;
    private long usageCount = 0L;
    private Cancellable schudelerCancellable;

    class Replenish {}

    @Override
    public void onReceive(Object message, ActorRef sender) {
        ActorRef self = TypedActor.context().self();
        if (message instanceof Replenish && self.equals(sender)) {
//            boolean b = self.equals(sender);
//            System.out.println("equals: "  + b);
            replenish();
        }

    }

    @Override
    public void postStop() {
        schudelerCancellable.cancel();
    }


    @Override
    public void preStart() {
        ActorSystem system = TypedActor.context().system();
        schudelerCancellable = system.scheduler().schedule(Duration.create(1, TimeUnit.SECONDS),
                Duration.create(1, TimeUnit.SECONDS),
                TypedActor.context().self(),
                new Replenish(),
                system.dispatcher(),
                TypedActor.context().self()
        );
        //( optionSelf().get(), new Replenish(), 1, 1, TimeUnit.SECONDS);
    }

    private void replenish() {
        ActorRef self = TypedActor.context().self();
        System.out.println("Actor " + self + " thread in replenish: " +
                Thread.currentThread().getName());
        if (level < MAXLEVEL) {
            level++;
        }
    }



    @Override
    public long getUnitsAvailable() {
        String self = TypedActor.context().self().toString();
//        System.out.println("actor: " + self +
//                " thread: " + Thread.currentThread().getName());
        return level;
    }

    @Override
    public long getUsageCount() {
        // emulate intensive calculations
//        try {
//            TimeUnit.SECONDS.sleep(3);
//        } catch (InterruptedException ignore) {}
        String self = TypedActor.context().self().toString();
//        System.out.println("actor: " + self +
//                " thread: " + Thread.currentThread().getName());
        return usageCount;

    }

    @Override
    public void useEnergy(final long units) {
        String self = TypedActor.context().self().toString();
        if (units > 0 && level - units >= 0) {
//            System.out.println("actor: " + self +
//                    " thread: " + Thread.currentThread().getName());
            level -= units;
            usageCount++;
        }
//        System.out.println("actor: " + self +
//                " thread: " + Thread.currentThread().getName() + " return from useEnergy");
    }
}
