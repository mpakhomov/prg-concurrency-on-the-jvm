package com.agiledeveloper.pcj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author mpakhomov
 * @since: 6/19/2015
 */
public class CompletionServiceNAV extends AbstractNAV {
    public double computeNetAssetValue(final Map<String, Integer> stocks)
            throws InterruptedException, ExecutionException {
        System.out.println(this.getClass().getName());
        final int numberOfCores = Runtime.getRuntime().availableProcessors();
        final double blockingCoefficient = 0.9;
        final int poolSize = (int) (numberOfCores / (1 - blockingCoefficient));

        System.out.println("Number of Cores available is " + numberOfCores);
        System.out.println("Pool size is " + poolSize);
        final List<Callable<Double>> partitions =
                new ArrayList<Callable<Double>>();
        for (final String ticker : stocks.keySet()) {
            partitions.add(new Callable<Double>() {
                public Double call() throws Exception {
                    return stocks.get(ticker) * YahooFinance.getPrice(ticker);
                }
            });
        }

        final ExecutorService executorPool =
                Executors.newFixedThreadPool(poolSize);
        final CompletionService<Double> completionService =
                new ExecutorCompletionService<Double>(executorPool);


        for (Callable<Double> callableTask : partitions) {
            completionService.submit(callableTask);
        }
        double netAssetValue = 0.0;
        for (int handledTasks = 0; handledTasks < partitions.size(); handledTasks++) {
            Future<Double> valueOfAStock = completionService.take();
            netAssetValue += valueOfAStock.get();
        }


//        final List<Future<Double>> valueOfStocks =
//                executorPool.invokeAll(partitions, 10000, TimeUnit.SECONDS);
//
//        double netAssetValue = 0.0;
//        for (final Future<Double> valueOfAStock : valueOfStocks)
//            netAssetValue += valueOfAStock.get();
//
        executorPool.shutdown();
        return netAssetValue;
    }

    public static void main(final String[] args)
            throws ExecutionException, InterruptedException, IOException {
        new CompletionServiceNAV().timeAndComputeValue();
    }
}
