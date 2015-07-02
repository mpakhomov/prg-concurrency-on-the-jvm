package com.agiledeveloper.pcj.create;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;

/**
 * @author mpakhomov
 * @since: 7/1/2015
 */
public class HollywoodActor extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private String name = "NoName";

    private HollywoodActor() {}

    private HollywoodActor(String name) {
        this.name = name;
    }

    /**
     * Create Props for an actor of this type.
     * @return a Props for creating this actor, which can then be further configured
     *         (e.g. calling `.withDispatcher()` on it)
     */
    public static Props props() {
        return Props.create(new Creator<HollywoodActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public HollywoodActor create() throws Exception {
                return new HollywoodActor();
            }
        });
    }

    public static Props props(String name) {
        return Props.create(new Creator<HollywoodActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public HollywoodActor create() throws Exception {
                return new HollywoodActor(name);
            }
        });
    }

    @Override
    public void onReceive(final Object role) {
        if (role instanceof  String) {
            log.info("Playing {} from Thread {}", role, Thread.currentThread().getName());
//            getSender().tell(role, getSelf());
        } else {
            log.error("Unknown message type");
            unhandled(role);
        }
    }
}
