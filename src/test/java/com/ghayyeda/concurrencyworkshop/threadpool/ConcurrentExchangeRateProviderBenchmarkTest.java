package com.ghayyeda.concurrencyworkshop.threadpool;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import java.util.concurrent.ExecutionException;


import static org.openjdk.jmh.annotations.Mode.AverageTime;

@State(Scope.Benchmark)
@BenchmarkMode(AverageTime)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
public class ConcurrentExchangeRateProviderBenchmarkTest {

    private final ConcurrentExchangeRateProvider concurrentExchangeRateProvider = new ConcurrentExchangeRateProvider(
            () -> sleepAndReturn(2),
            6,
            () -> sleepAndReturn(1),
            6,
            () -> sleepAndReturn(3),
            6,
            () -> sleepAndReturn(7),
            6
    );

    @TearDown
    public void tearDown() {
        concurrentExchangeRateProvider.close();
    }

    @Test
    public void benchmarkRunner() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(this.getClass().getName())
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    @Threads(6)
    public void testAverageTime(Blackhole blackhole) throws InterruptedException, ExecutionException {
        blackhole.consume(concurrentExchangeRateProvider.getExchangeRate().get());
    }

    private double sleepAndReturn(double returnValue) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return returnValue;
    }

}