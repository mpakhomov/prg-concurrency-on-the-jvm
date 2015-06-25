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
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

public class UseEnergySource {
  //private EnergySource energySource = new EnergySource();
  private EnergySource energySource = EnergySource.create();
  
  public long drainInOneUnits(int units) {
    for(int i = 0; i < units; i++) {
      energySource.useEnergy(1);
    }
    return getEnergyLevel();
  }
  
  public long getEnergyLevel() {
    return energySource.getUnitsAvailable();
  }
  
  public void close() {
    energySource.stopEnergySource();
  }
  
  public static void main(final String[] args) throws InterruptedException, ExecutionException {
    final UseEnergySource user = new UseEnergySource();
    System.out.println(user.getEnergyLevel());
        
    List<Callable<Long>> tasks = new ArrayList<Callable<Long>>();
    
    for(int i = 0; i < 10; i++) {
      tasks.add(new Callable<Long>() {
        public Long call() { return user.drainInOneUnits(7); }
      });
    }
    
    ExecutorService service = Executors.newFixedThreadPool(10);
    service.invokeAll(tasks);
    
    System.out.println(user.getEnergyLevel());

    Thread.sleep(10000);    

    System.out.println(user.getEnergyLevel());

    service.shutdown();
    
    user.close();
  }
}
