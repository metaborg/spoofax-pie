package mb.spoofax.runtime.benchmark.incremental;

import mb.spoofax.runtime.benchmark.state.*;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class ObservingExecIncrBenchmark {
    @Setup(Level.Trial)
    public void setupTrial(SpoofaxPieState spoofaxPie, WorkspaceState workspace, IncrState incr, InfraState infra, ObservingExecState exec) throws IOException {
        workspace.setup(spoofaxPie);
        incr.setup(workspace);
        infra.setup(spoofaxPie, workspace);
        exec.setup(spoofaxPie, workspace, infra);

        this.spoofaxPie = spoofaxPie;
        this.workspace = workspace;
        this.incr = incr;
        this.infra = infra;
        this.exec = exec;

        exec.exec(workspace, Collections.emptyList());
    }

    private SpoofaxPieState spoofaxPie;
    private WorkspaceState workspace;
    private IncrState incr;
    private InfraState infra;
    private ObservingExecState exec;

    @Setup(Level.Invocation) public void setupInvocation() throws IOException {
        infra.renew(spoofaxPie);
        incr.renew();
        exec.renew(spoofaxPie, workspace, infra);
    }

    @Benchmark public void exec() {
        exec.exec(workspace, incr.allChangedPaths);
    }
}
