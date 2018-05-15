package mb.spoofax.runtime.benchmark;

import mb.pie.runtime.impl.logger.Trace;
import org.jetbrains.annotations.Nullable;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PieProfiler implements InternalProfiler {
    private static boolean enabled = false;
    private static @Nullable List<Trace> traces = null;


    public PieProfiler() {
        PieProfiler.enabled = true;
    }


    public static boolean isEnabled() {
        return PieProfiler.enabled;
    }

    public static void setTraces(List<Trace> traces) {
        PieProfiler.traces = traces;
    }


    @Override public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {
        PieProfiler.traces = null;
    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams, IterationResult result) {
        if(PieProfiler.traces == null) {
            return Collections.emptyList();
        }

        final ArrayList<? extends Result> results = new ArrayList<>();
        return results;
    }


    @Override public String getDescription() {
        return "PIE Profiler";
    }
}
