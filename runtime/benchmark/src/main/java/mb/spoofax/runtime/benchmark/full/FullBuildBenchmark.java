package mb.spoofax.runtime.benchmark.full;

import mb.spoofax.runtime.benchmark.counter.PieCounters;
import mb.spoofax.runtime.benchmark.state.SpoofaxPieState;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
public class FullBuildBenchmark {
    @Benchmark
    public Object fullBuild(SpoofaxPieState state, PieCounters counters) {
        return state.runBuild();
    }
}
