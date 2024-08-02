package com.ghayyeda.concurrencyworkshop.exchangerates;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


import static java.util.concurrent.CompletableFuture.supplyAsync;

public class ConcurrentExchangeRateProvider implements AutoCloseable {

    private final BankExchangesRatesRepository repository1;

    private final BankExchangesRatesRepository repository2;

    private final BankExchangesRatesRepository repository3;

    private final BankExchangesRatesRepository repository4;

    private final ExecutorService requestExecutor;

    private final Semaphore semaphore1;

    private final Semaphore semaphore2;

    private final Semaphore semaphore3;

    private final Semaphore semaphore4;

    public ConcurrentExchangeRateProvider(
            BankExchangesRatesRepository bank1Repository,
            int bank1ConcurrentCallsLimit,
            BankExchangesRatesRepository bank2Repository,
            int bank2ConcurrentCallsLimit,
            BankExchangesRatesRepository bank3Repository,
            int bank3ConcurrentCallsLimit,
            BankExchangesRatesRepository bank4Repository,
            int bank4ConcurrentCallsLimit
    ) {
        this.repository1 = bank1Repository;
        this.repository2 = bank2Repository;
        this.repository3 = bank3Repository;
        this.repository4 = bank4Repository;
        this.requestExecutor = Executors.newFixedThreadPool(
                bank1ConcurrentCallsLimit + bank2ConcurrentCallsLimit + bank4ConcurrentCallsLimit + bank4ConcurrentCallsLimit);
        this.semaphore1 = new Semaphore(bank1ConcurrentCallsLimit);
        this.semaphore2 = new Semaphore(bank2ConcurrentCallsLimit);
        this.semaphore3 = new Semaphore(bank3ConcurrentCallsLimit);
        this.semaphore4 = new Semaphore(bank4ConcurrentCallsLimit);
    }

    public Future<Double> getExchangeRate() {
        return supplyAsync(getExchangeRate(repository1::getExchangeRate, semaphore1), requestExecutor)
                .thenCombine(supplyAsync(getExchangeRate(repository2::getExchangeRate, semaphore2), requestExecutor), Double::sum)
                .thenCombine(supplyAsync(getExchangeRate(repository3::getExchangeRate, semaphore3), requestExecutor), Double::sum)
                .thenCombine(supplyAsync(getExchangeRate(repository4::getExchangeRate, semaphore4), requestExecutor), Double::sum)
                .thenApply(sum -> sum / 4);
    }

    private Supplier<Double> getExchangeRate(Supplier<Double> delegate, Semaphore semaphore) {
        return () -> {
            if (semaphore.tryAcquire()) {
                try {
                    return delegate.get();
                } finally {
                    semaphore.release();
                }
            } else {
                throw new TooManyRequestsException();
            }
        };
    }

    @Override
    public void close() {
        requestExecutor.shutdown();
        try {
            if (!requestExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                requestExecutor.shutdownNow();
                if (!requestExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Couldn't properly shutdown requestsExecutor");
                }
            }
        } catch (InterruptedException e) {
            requestExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
