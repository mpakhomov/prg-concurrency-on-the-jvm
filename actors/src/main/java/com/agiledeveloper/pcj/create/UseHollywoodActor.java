/***
 * Excerpted from "Programming Concurrency on the JVM",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/vspcon for more book information.
 ***/
package com.agiledeveloper.pcj.create;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
//import akka.actor.Actors;

public class UseHollywoodActor {
    public static void main(final String[] args) throws InterruptedException {
        final ActorSystem system = ActorSystem.create("MySystem");
        final ActorRef johnnyDepp = system.actorOf(HollywoodActor.props("Jonny Depp"), "JonnyDeppActor");

        johnnyDepp.tell("Jack Sparrow", ActorRef.noSender());
        Thread.sleep(100);
        johnnyDepp.tell("Edward Scissorhands", ActorRef.noSender());
        Thread.sleep(100);
        johnnyDepp.tell("Willy Wonka", ActorRef.noSender());

        johnnyDepp.tell(1L, ActorRef.noSender());
        system.shutdown();
    }
}
