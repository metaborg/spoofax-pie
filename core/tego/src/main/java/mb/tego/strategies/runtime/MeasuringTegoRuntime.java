package mb.tego.strategies.runtime;

import mb.log.api.LoggerFactory;
import mb.tego.sequences.MeasuringSeq;
import mb.tego.sequences.Seq;
import mb.tego.strategies.StrategyDecl;
import mb.tego.strategies.TegoScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * A measuring Tego runtime.
 */
@TegoScope
public final class MeasuringTegoRuntime extends TegoRuntimeImpl {

    /**
     * Initializes a new instance of the {@link TegoRuntimeImpl} class.
     *
     * @param loggerFactory the logger factory
     */
    @Inject public MeasuringTegoRuntime(@Nullable LoggerFactory loggerFactory) {
        super(loggerFactory);
    }

    private final HashMap<String, StrategyTime> timings = new HashMap<>();
    private final ArrayDeque<Long> currentStrategyTime = new ArrayDeque<>();

    public Map<String, StrategyTime> getMeasurements() {
        return this.timings;
    }

    @Override
    protected void enterStrategy(StrategyDecl strategy) {
        if (!strategy.isAnonymous()) {
            // Only measure time for named strategies
            long startTime = System.nanoTime();
            currentStrategyTime.push(startTime);
        }
        super.enterStrategy(strategy);
    }

    @Override
    protected <R> @Nullable R exitStrategy(StrategyDecl strategy, @Nullable R result) {
        R newResult = result;
        if (!strategy.isAnonymous()) {
            // Only measure time for named strategies
            long endTime = System.nanoTime();
            long startTime = currentStrategyTime.pop();

            timings.compute(strategy.getName(), (k, v) -> {
                if (v == null) v = new StrategyTime();
                v.addStrategyTime(endTime - startTime);
                return v;
            });

            if (result instanceof Seq) {
                //noinspection unchecked
                newResult = (R)new MeasuringSeq<>((Seq<Object>)result, (t, e) -> {
                    timings.compute(strategy.getName(), (k, v) -> {
                        if (v == null) v = new StrategyTime();
                        v.addResultTime(t);
                        return v;
                    });
                });
            }
        }
        return super.exitStrategy(strategy, newResult);
    }

    public static class StrategyTime {
        private long strategyTime = 0;
        private long resultTime = 0;

        /* package private */ StrategyTime addStrategyTime(long nsTime) {
            this.strategyTime += nsTime;
            return this;
        }

        /* package private */ StrategyTime addResultTime(long nsTime) {
            this.resultTime += nsTime;
            return this;
        }

        public long getStrategyTime() {
            return strategyTime;
        }

        public long getResultTime() {
            return resultTime;
        }

        @Override public String toString() {
            return "{" +
                "strategy: " + (strategyTime / 1000000.0) +
                " ms, result: " + (resultTime / 1000000.0) +
                "ms }";
        }
    }
}


