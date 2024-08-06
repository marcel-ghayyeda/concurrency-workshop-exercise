package com.ghayyeda.concurrencyworkshop.exchangerates;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

@State(Scope.Benchmark)
@BenchmarkMode(AverageTime)
@Fork(1)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
public class SetupTest {

    @Test
    void shouldRunJUnit() {
        assertEquals(2.5, 2.5, "Sum should be 2.5");
    }
    @Test
    public void shouldRunJmh() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(this.getClass().getName())
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    @Threads(2)
    public void testAverageTime(Blackhole blackhole) {
        Blackhole.consumeCPU(10);
    }


}