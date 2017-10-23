package mb.spoofax.runtime.benchmark.full;

import mb.spoofax.runtime.benchmark.state.InfraState;
import mb.spoofax.runtime.benchmark.state.PullingExecState;
import mb.spoofax.runtime.benchmark.state.SpoofaxPieState;
import mb.spoofax.runtime.benchmark.state.WorkspaceState;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class PullingFullExecBenchmark {
    @Setup(Level.Invocation)
    public void setup(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState, PullingExecState pullingExecState) throws IOException {
        workspaceState.setup(spoofaxPieState);
        infraState.setup(spoofaxPieState, workspaceState);
        pullingExecState.setup(infraState);

        this.spoofaxPieState = spoofaxPieState;
        this.workspaceState = workspaceState;
        this.infraState = infraState;
        this.pullingExecState = pullingExecState;
    }

    private SpoofaxPieState spoofaxPieState;
    private WorkspaceState workspaceState;
    private InfraState infraState;
    private PullingExecState pullingExecState;

    @Benchmark public Object exec() {
        return pullingExecState.exec(workspaceState);
    }
}
