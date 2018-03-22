package mb.spoofax.runtime.benchmark.full;

import mb.spoofax.runtime.benchmark.state.InfraState;
import mb.spoofax.runtime.benchmark.state.exec.TDState;
import mb.spoofax.runtime.benchmark.state.SpoofaxPieState;
import mb.spoofax.runtime.benchmark.state.WorkspaceState;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class TDFullBench {
    @Setup(Level.Invocation)
    public void setup(SpoofaxPieState spoofaxPie, WorkspaceState workspace, InfraState infra, TDState exec) throws IOException {
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
    private TDState exec;

    @Benchmark public Object exec() {
        return exec.exec(workspace);
    }
}
