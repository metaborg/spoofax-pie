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
public class PullingExecFullBenchmark {
    @Setup(Level.Invocation)
    public void setup(SpoofaxPieState spoofaxPie, WorkspaceState workspace, InfraState infra, PullingExecState exec) throws IOException {
        workspace.setup(spoofaxPie);
        infra.setup(spoofaxPie, workspace);
        exec.setup(infra);

        this.spoofaxPie = spoofaxPie;
        this.workspace = workspace;
        this.infra = infra;
        this.exec = exec;
    }

    private SpoofaxPieState spoofaxPie;
    private WorkspaceState workspace;
    private InfraState infra;
    private PullingExecState exec;

    @Benchmark public Object exec() {
        return exec.exec(workspace);
    }
}
