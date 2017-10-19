package mb.spoofax.runtime.benchmark.incremental;

import mb.spoofax.runtime.benchmark.state.SpoofaxPieState;
import mb.spoofax.runtime.benchmark.counter.PieCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
public class IncrBuildBenchmark {
    @Benchmark public Object incrementalBuild(SpoofaxPieState state, PieCounters counters) {
        return state.runBuild();
    }
}
