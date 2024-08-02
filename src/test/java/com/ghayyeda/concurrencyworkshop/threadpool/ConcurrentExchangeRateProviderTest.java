package com.ghayyeda.concurrencyworkshop.threadpool;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcurrentExchangeRateProviderTest {

    @Test
    void shouldCalculateAverageExchangeRate() throws InterruptedException, ExecutionException {
        ConcurrentExchangeRateProvider concurrentSummer = new ConcurrentExchangeRateProvider(
                () -> sleepAndReturn(500, 1.0),
                1,
                () -> sleepAndReturn(500, 2.0),
                1,
                () -> sleepAndReturn(500, 3.0),
                1,
                () -> sleepAndReturn(500, 4.0),
                1
        );

        Future<Double> sum = concurrentSummer.getExchangeRate();
        assertEquals(2.5, sum.get(), "Sum should be 2.5");
    }

    @Test
    void shouldProcessFurtherRequestsAfterException(@Mock BankExchangesRatesRepository repository1) throws InterruptedException, ExecutionException {

        when(repository1.getExchangeRate()).then(
                new Answer<Double>() {
                    int callNumber = 0;

                    @Override
                    public Double answer(InvocationOnMock invocation) throws Throwable {
                        callNumber++;
                        if (callNumber == 1) {
                            throw new RuntimeException("Simulated API call exception");
                        }
                        return sleepAndReturn(500, 1.0);
                    }
                }
        );
        ConcurrentExchangeRateProvider concurrentSummer = new ConcurrentExchangeRateProvider(
                repository1,
                1,
                () -> sleepAndReturn(500, 2.0),
                1,
                () -> sleepAndReturn(500, 3.0),
                1,
                () -> sleepAndReturn(500, 4.0),
                1
        );
        assertThrows(RuntimeException.class, () -> callAndWrapException(concurrentSummer).get());
        Thread.sleep(550); //wait shortly to make sure all previous requests finished
        assertEquals(2.5, concurrentSummer.getExchangeRate().get(), "Sum should be 2.5");
    }

    @Test
    void shouldAllowAtMost6ConcurrentRequestsToEachRepository() {
        ConcurrentExchangeRateProvider concurrentSummer = new ConcurrentExchangeRateProvider(
                () -> sleepAndReturn(500, 1.0),
                6,
                () -> sleepAndReturn(500, 2.0),
                6,
                () -> sleepAndReturn(500, 3.0),
                6,
                () -> sleepAndReturn(500, 4.0),
                6
        );

        ExecutorService executorService = Executors.newFixedThreadPool(6);
        CompletableFuture.allOf(
                CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService)
        ).join();
    }

    private static Supplier<Double> callAndWrapException(ConcurrentExchangeRateProvider concurrentSummer) {
        return () -> {
            try {
                return concurrentSummer.getExchangeRate().get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                }
                throw new RuntimeException(e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        };
    }

    @Disabled
    @Test
    //Bonus test for performance checking
    void shouldNotAttemptToFetchFromRepositoriesWhenAtLeastOneIsAtCapacity(@Mock BankExchangesRatesRepository repository1) {
        ConcurrentExchangeRateProvider concurrentSummer = new ConcurrentExchangeRateProvider(
                repository1,
                10,
                () -> sleepAndReturn(500, 2.0),
                10,
                () -> sleepAndReturn(500, 3.0),
                10,
                () -> sleepAndReturn(500, 4.0),
                0
        );

        Future<Double> futureResult1 = concurrentSummer.getExchangeRate();
        Future<Double> futureResult2 = concurrentSummer.getExchangeRate();
        try {
            futureResult1.get();
            futureResult2.get();
        } catch (InterruptedException | ExecutionException e) {
            //we don't care in this case, we just want to verify that repository was not pointlessly invoked
        }
        verify(repository1, never()).getExchangeRate();
    }

    @ParameterizedTest()
    @CsvSource({
            "1,6,6,6",
            "6,6,6,6",
            //"0,1,1,1" // bonus ;)
    })
    void shouldNotAllowTooManyConcurrentRequestsToRepositories(int limit1, int limit2, int limit3, int limit4) {
        ConcurrentExchangeRateProvider concurrentSummer = new ConcurrentExchangeRateProvider(
                () -> sleepAndReturn(500, 1.0),
                limit1,
                () -> sleepAndReturn(500, 2.0),
                limit2,
                () -> sleepAndReturn(500, 3.0),
                limit3,
                () -> sleepAndReturn(500, 4.0),
                limit4
        );

        ExecutorService executorService = Executors.newFixedThreadPool(15);
        try {
            CompletableFuture.allOf(
                    CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                    CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                    CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                    CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                    CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                    CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                    CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                    CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                    CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService),
                    CompletableFuture.supplyAsync(callAndWrapException(concurrentSummer), executorService)
            ).join();
        } catch (CompletionException e) {
            assertEquals(TooManyRequestsException.class, e.getCause().getClass(), "Should throw TooManyRequestsException");
            return;
        }
        throw new AssertionError("Should throw TooManyRequestsException");
    }

    private double sleepAndReturn(int sleepMillis, double returnValue) {
        sleep(sleepMillis);
        return returnValue;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

}