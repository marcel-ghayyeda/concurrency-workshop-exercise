package com.ghayyeda.concurrencyworkshop.multipleservers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


import static java.util.concurrent.CompletableFuture.supplyAsync;

public class DataFetcher {

    private final String cachedData;

    private final Server server1;

    private final Server server2;

    private final DataTransformer dataTransformer;

    public DataFetcher(String cachedData, Server server1, Server server2, DataTransformer dataTransformer) {
        this.cachedData = cachedData;
        this.server1 = server1;
        this.server2 = server2;
        this.dataTransformer = dataTransformer;
    }

    CompletableFuture<String> fetch() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        return fetchHandlingExceptions(executorService);
    }

    private CompletableFuture<String> basicFetch(ExecutorService executorService) {
        return supplyAsync(server1::fetchData, executorService)
                .applyToEither(supplyAsync(server2::fetchData, executorService), Function.identity())
                .thenApplyAsync(dataTransformer::transform, executorService)
                .completeOnTimeout(cachedData, 1, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    System.out.println("Execution failed");
                    return cachedData;
                });
    }

    private CompletableFuture<String> fetchHandlingExceptions(ExecutorService executorService) {
        return either(
                supplyAsync(server1::fetchData, executorService),
                supplyAsync(server2::fetchData, executorService))
                .thenApplyAsync(dataTransformer::transform, executorService)
                .completeOnTimeout(cachedData, 1, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    System.out.println("Execution failed");
                    return cachedData;
                });
    }

    /*
     * source: https://4comprehension.com/be-careful-with-completablefuture-applytoeither/
     */
    private <T> CompletableFuture<T> either(CompletableFuture<T> f1, CompletableFuture<T> f2) {
        CompletableFuture<T> result = new CompletableFuture<>();
        CompletableFuture.allOf(f1, f2).whenComplete((__, throwable) -> {
            if (f1.isCompletedExceptionally() && f2.isCompletedExceptionally()) {
                result.completeExceptionally(throwable);
            }
        });

        f1.thenAccept(result::complete);
        f2.thenAccept(result::complete);
        return result;
    }
}
