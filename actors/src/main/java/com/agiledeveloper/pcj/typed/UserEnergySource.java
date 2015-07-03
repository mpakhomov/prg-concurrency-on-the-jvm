package com.agiledeveloper.pcj.typed;

import akka.actor.TypedActor;
import akka.actor.TypedActorExtension;
import akka.actor.TypedActor;
import akka.actor.*;
import akka.japi.*;
import akka.dispatch.Futures;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;



/**
 * @author mpakhomov
 * @since: 7/3/2015
 */
public class UserEnergySource {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Thread in main: " +
                Thread.currentThread().getName());

        final ActorSystem system = ActorSystem.create("MySystem");

        //Returns the Typed Actor Extension
        final TypedActorExtension typed =
                TypedActor.get(system); //system is an instance of ActorSystem
        final EnergySource energySource =
                TypedActor.get(system).typedActorOf(
                        new TypedProps<EnergySourceImpl>(EnergySource.class, EnergySourceImpl.class),
                        "energySourceActor");

        final EnergySource energySource2 =
                TypedActor.get(system).typedActorOf(
                        new TypedProps<EnergySourceImpl>(EnergySource.class, EnergySourceImpl.class),
                        "energySourceActor2");

        System.out.println("begin");
        long l = energySource.getUnitsAvailable();
        System.out.println("end");
//        System.exit(0);
        System.out.println("Energy units " + energySource.getUnitsAvailable());
        System.out.println("Energy units " + energySource2.getUnitsAvailable());

        System.out.println("Firing two requests for use energy");
        energySource.useEnergy(10);
        energySource.useEnergy(10);
        System.out.println("Fired two requests for use energy");
        Thread.sleep(100);
        System.out.println("Firing one more requests for use energy");
        energySource.useEnergy(10);
        energySource2.useEnergy(15);

        Thread.sleep(1000);
        System.out.println("Energy units " + energySource.getUnitsAvailable());
        System.out.println("Energy units " + energySource2.getUnitsAvailable());
        System.out.println("Usage " + energySource.getUsageCount());
        System.out.println("Usage " + energySource2.getUsageCount());

        typed.stop(energySource);
        typed.poisonPill(energySource2);
        system.shutdown();
    }
}
