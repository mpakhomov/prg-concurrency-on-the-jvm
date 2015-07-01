package com.agiledeveloper.pcj.create;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;

/**
 * @author mpakhomov
 * @since: 7/1/2015
 */
public class HollywoodActor extends UntypedActor {
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

    @Override
    public void onReceive(final Object role) {
        System.out.println("Playing " + role +
                " from Thread " + Thread.currentThread().getName());
    }
}
