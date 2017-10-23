package mb.spoofax.runtime.benchmark.incremental;

import mb.spoofax.runtime.benchmark.state.*;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class PullingIncrExecBenchmark {
    @Setup(Level.Trial)
    public void setupTrial(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, IncrState incrState, InfraState infraState, PullingExecState pullingExecState) throws IOException {
        workspaceState.setup(spoofaxPieState);
        incrState.setup(workspaceState);
        infraState.setup(spoofaxPieState, workspaceState);
        pullingExecState.setup(infraState);

        this.spoofaxPieState = spoofaxPieState;
        this.workspaceState = workspaceState;
        this.incrState = incrState;
        this.infraState = infraState;
        this.pullingExecState = pullingExecState;

        pullingExecState.exec(workspaceState);
    }

    private SpoofaxPieState spoofaxPieState;
    private WorkspaceState workspaceState;
    private IncrState incrState;
    private InfraState infraState;
    private PullingExecState pullingExecState;

    @Setup(Level.Invocation) public void setupInvocation() {
        infraState.renew(spoofaxPieState);
        pullingExecState.renew(infraState);
    }

    @Benchmark public Object exec() {
        return pullingExecState.exec(workspaceState);
    }
}
