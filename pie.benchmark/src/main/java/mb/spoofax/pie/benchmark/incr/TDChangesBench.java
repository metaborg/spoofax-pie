package mb.spoofax.pie.benchmark.incr;

import mb.spoofax.pie.benchmark.Timer;
import mb.spoofax.pie.benchmark.state.*;
import mb.spoofax.pie.benchmark.state.exec.TDState;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class TDChangesBench {
    @Setup(Level.Trial)
    public void setupTrial(SpoofaxPieState spoofaxPie, WorkspaceState workspace, InfraState infra, ChangesState changes, TDState exec) {
        workspace.setup(spoofaxPie);
        infra.setup(spoofaxPie, workspace);
        changes.setup(workspace);
        exec.setup(spoofaxPie, workspace, infra);

        this.spoofaxPie = spoofaxPie;
        this.workspace = workspace;
        this.infra = infra;
        this.changes = changes;
        this.exec = exec;
    }

    private SpoofaxPieState spoofaxPie;
    private WorkspaceState workspace;
    private InfraState infra;
    private ChangesState changes;
    private TDState exec;

    @Setup(Level.Invocation) public void setupInvocation() {
        Timer.logFile = new File("/Users/gohla/pie/topdown.csv");
        Timer.clearFile();
        infra.reset();
        changes.reset(workspace);
        exec.setup(spoofaxPie, workspace, infra);
        exec.reset();
    }

    @Benchmark public void exec(Blackhole blackhole) {
        changes.exec(exec, blackhole);
    }

    @TearDown(Level.Trial) public void tearDownTrial() {
        infra.reset();
        changes.reset(workspace);
        exec.reset();
    }
}
