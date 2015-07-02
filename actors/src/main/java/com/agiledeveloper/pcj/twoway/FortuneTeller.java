/***
 * Excerpted from "Programming Concurrency on the JVM",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/vspcon for more book information.
 ***/
package com.agiledeveloper.pcj.twoway;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.concurrent.TimeUnit;

public class FortuneTeller extends UntypedActor {
    public static Props props() {
        return Props.create(new Creator<FortuneTeller>() {
            private static final long serialVersionUID = 1L;

            @Override
            public FortuneTeller create() throws Exception {
                return new FortuneTeller();
            }
        });
    }

    public void onReceive(final Object name) {
        //getContext().replyUnsafe(String.format("%s you'll rock", name));
        getSender().tell(String.format("%s you'll rock", name), getSelf());
    }

    public static void main(final String[] args) {
//        final ActorRef fortuneTeller =
//                Actors.actorOf(FortuneTeller.class).start();
        final ActorSystem system = ActorSystem.create("MySystem");
        final ActorRef fortuneTeller = system.actorOf(FortuneTeller.props(), "FortuneTeller");
        Timeout t = new Timeout(5, TimeUnit.SECONDS);
        Future<Object> future = Patterns.ask(fortuneTeller, "Joe", t);
        try {
            String response = (String) Await.result(future, t.duration());
            System.out.println("Response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Never got a response before timeout");
        }

        system.shutdown();

//        try {
//            final Object response = fortuneTeller.sendRequestReply("Joe");
//            System.out.println(response);
//        } catch (ActorTimeoutException ex) {
//            System.out.println("Never got a response before timeout");
//        } finally {
//            fortuneTeller.stop();
//        }
//        ActorRef ref = sys.actorOf(Props.create(TestActor.class), "mytest");
//        Timeout t = new Timeout(5, TimeUnit.SECONDS);
//        Future<Object> fut = Patterns.ask(ref, "foo", t);
//        String response = (String)Await.result(fut, t.duration());
//        System.out.println(response);
    }
}
