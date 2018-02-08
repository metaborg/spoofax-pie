package mb.spoofax.runtime.benchmark.incremental;

import mb.spoofax.runtime.benchmark.state.*;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class TopDownExecIncrBenchmark {
    @Setup(Level.Trial)
    public void setupTrial(SpoofaxPieState spoofaxPie, WorkspaceState workspace, IncrState incr, InfraState infra, PullingExecState exec) throws IOException {
        workspace.setup(spoofaxPie);
        incr.setup(workspace);
        infra.setup(spoofaxPie, workspace);
        exec.setup(infra);

        this.spoofaxPie = spoofaxPie;
        this.workspace = workspace;
        this.incr = incr;
        this.infra = infra;
        this.exec = exec;

        exec.exec(workspace);
    }

    private SpoofaxPieState spoofaxPie;
    private WorkspaceState workspace;
    private IncrState incr;
    private InfraState infra;
    private PullingExecState exec;

    @Setup(Level.Invocation) public void setupInvocation() {
        infra.renew(spoofaxPie);
        exec.renew(infra);
    }

    @Benchmark public Object exec() {
        return exec.exec(workspace);
    }
}
