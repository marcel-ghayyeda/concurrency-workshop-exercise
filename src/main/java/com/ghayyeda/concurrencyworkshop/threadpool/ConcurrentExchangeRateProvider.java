package com.ghayyeda.concurrencyworkshop.threadpool;

import java.util.concurrent.Future;

public class ConcurrentExchangeRateProvider implements AutoCloseable {

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

    }

    public Future<Double> getExchangeRate() throws TooManyRequestsException {
        //TODO: calculate the average exchange rate from all banks (repositories)
        throw new UnsupportedOperationException("Implement me!");
    }

    @Override
    public void close() {
    }
}
