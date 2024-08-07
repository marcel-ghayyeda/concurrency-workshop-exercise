package com.ghayyeda.concurrencyworkshop.multipleservers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import static org.junit.jupiter.api.Assertions.assertEquals;

class DataFetcherTest {

    private static final DataTransformer dataTransformer = rawData -> new String(rawData, StandardCharsets.UTF_8);

    @Test
    void shouldReturnDataFromFastestServer() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        Server fastServer = () -> {
            return "fast-server".getBytes(StandardCharsets.UTF_8);
        };

        Server slowServer = () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException();
            }
            return "slow-server".getBytes(StandardCharsets.UTF_8);
        };
        DataFetcher dataFetcher = new DataFetcher("cached-data", slowServer, fastServer, dataTransformer);

        //when
        CompletableFuture<String> result = dataFetcher.fetch();

        //then
        assertEquals("fast-server", result.get(300, TimeUnit.MILLISECONDS));
    }

    @Disabled
    @Test
    void shouldReturnDataFromWorkingServerWhenOtherFails() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        Server failingServer = () -> {
            throw new IllegalStateException();
        };

        Server slowServer = () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException();
            }
            return "slow-server".getBytes(StandardCharsets.UTF_8);
        };
        DataFetcher dataFetcher = new DataFetcher("cached-data", slowServer, failingServer, dataTransformer);

        //when
        CompletableFuture<String> result = dataFetcher.fetch();

        //then
        assertEquals("slow-server", result.get(300, TimeUnit.MILLISECONDS));
    }

    @Test
    void shouldReturnDataCachedDataWhenAllFail() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        Server server1 = () -> {
            throw new IllegalStateException();
        };

        Server server2 = () -> {
            throw new IllegalStateException();
        };

        DataFetcher dataFetcher = new DataFetcher("cached-data", server1, server2, dataTransformer);

        //when
        CompletableFuture<String> result = dataFetcher.fetch();

        //then
        assertEquals("cached-data", result.get(300, TimeUnit.MILLISECONDS));
    }

    @Test
    void shouldReturnCachedDataOnTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        Server server = () -> {
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException();
            }
            return "slow-server".getBytes(StandardCharsets.UTF_8);
        };

        DataFetcher dataFetcher = new DataFetcher("cached-data", server, server, dataTransformer);

        //when
        CompletableFuture<String> result = dataFetcher.fetch();

        //then
        assertEquals("cached-data", result.get(1100, TimeUnit.MILLISECONDS));
    }
}