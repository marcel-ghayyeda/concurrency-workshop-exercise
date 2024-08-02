package com.ghayyeda.concurrencyworkshop.exchangerates;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    //Do not modify signature of this constructor - it's used in tests
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
                bank1ConcurrentCallsLimit + bank2ConcurrentCallsLimit + bank3ConcurrentCallsLimit + bank4ConcurrentCallsLimit);
        this.semaphore1 = new Semaphore(bank1ConcurrentCallsLimit, true);
        this.semaphore2 = new Semaphore(bank2ConcurrentCallsLimit, true);
        this.semaphore3 = new Semaphore(bank3ConcurrentCallsLimit, true);
        this.semaphore4 = new Semaphore(bank4ConcurrentCallsLimit, true);
    }

    public Future<Double> getExchangeRate() {
        try {
            Set<Future<Double>> futureResults = Set.of(
                    requestExecutor.submit(() -> get(repository1::getExchangeRate, semaphore1)),
                    requestExecutor.submit(() -> get(repository2::getExchangeRate, semaphore2)),
                    requestExecutor.submit(() -> get(repository3::getExchangeRate, semaphore3)),
                    requestExecutor.submit(() -> get(repository4::getExchangeRate, semaphore4))
            );
            return new AverageCalculatingFuture(futureResults);

        } catch (RejectedExecutionException rejectedExecutionException) {
            return new FailedFuture<>(new TooManyRequestsException());
        }
    }

    private Double get(Supplier<Double> delegate, Semaphore semaphore) {
        if (semaphore.tryAcquire()) {
            try {
                return delegate.get();
            } finally {
                semaphore.release();
            }
        } else {
            throw new TooManyRequestsException();
        }
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

    private static class AverageCalculatingFuture implements Future<Double> {

        private final Set<Future<Double>> futureResults;

        public AverageCalculatingFuture(Set<Future<Double>> futureResults) {
            this.futureResults = futureResults;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            Set<Boolean> cancellationResults = futureResults.stream().map(future -> future.cancel(mayInterruptIfRunning)).collect(Collectors.toSet());
            return !cancellationResults.contains(false);
        }

        @Override
        public boolean isCancelled() {
            return futureResults.stream().anyMatch(Future::isCancelled);
        }

        @Override
        public boolean isDone() {
            return futureResults.stream().allMatch(Future::isDone);
        }

        @Override
        public Double get() throws InterruptedException, ExecutionException {
            double sum = 0;
            for (Future<Double> futureResult : futureResults) {
                sum += futureResult.get();
            }
            return sum / 4;
        }

        @Override
        public Double get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            double sum = 0;
            for (Future<Double> futureResult : futureResults) {
                sum += futureResult.get(timeout, unit);
            }
            return sum / 4;
        }
    }

    public static class FailedFuture<T> implements Future<T> {

        private final Throwable throwable;

        public FailedFuture(Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() throws ExecutionException {
            throw new ExecutionException(throwable);
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws ExecutionException {
            return get();
        }
    }
}
