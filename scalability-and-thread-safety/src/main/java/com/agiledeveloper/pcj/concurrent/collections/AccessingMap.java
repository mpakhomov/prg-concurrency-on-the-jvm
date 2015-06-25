/***
 * Excerpted from "Programming Concurrency on the JVM",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/vspcon for more book information.
 ***/
package com.agiledeveloper.pcj.concurrent.collections;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class AccessingMap {
    private static void useMap(final Map<String, Integer> scores)
            throws InterruptedException {
        scores.put("Fred", 10);
        scores.put("Sara", 12);

        try {
            for (final String key : scores.keySet()) {
                System.out.println(key + " score " + scores.get(key));
                scores.put("Joe", 14);
            }
        } catch (Exception ex) {
            System.out.println("Failed: " + ex);
        }

        System.out.println("Number of elements in the map: " +
                scores.keySet().size());
    }

    public static void main(final String[] args) throws InterruptedException {
        System.out.println("Using Plain vanilla HashMap");
        useMap(new HashMap<String, Integer>());

        System.out.println("Using Synchronized HashMap");
        useMap(Collections.synchronizedMap(new HashMap<String, Integer>()));

        System.out.println("Using Concurrent HashMap");
        useMap(new ConcurrentHashMap<String, Integer>());
    }
}
