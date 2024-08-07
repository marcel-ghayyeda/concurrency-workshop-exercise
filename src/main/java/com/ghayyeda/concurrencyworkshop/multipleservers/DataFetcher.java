package com.ghayyeda.concurrencyworkshop.multipleservers;

import java.util.concurrent.CompletableFuture;

public class DataFetcher {

    private final String cachedData;

    private final Server server1;

    private final Server server2;

    private final DataTransformer dataTransformer;

    //Do not modify signature of this constructor - it's used in tests
    public DataFetcher(String cachedData, Server server1, Server server2, DataTransformer dataTransformer) {
        this.cachedData = cachedData;
        this.server1 = server1;
        this.server2 = server2;
        this.dataTransformer = dataTransformer;
    }

    CompletableFuture<String> fetch() {
        //TODO Your solution goes here :)
        throw new UnsupportedOperationException("Implement me!");
    }

}
