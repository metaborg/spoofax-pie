package mb.spoofax.runtime.benchmark.state.exec;

import kotlin.Unit;
import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.exec.BottomUpTopsortExecutor;
import mb.pie.runtime.core.impl.exec.BottomUpTopsortExecutorImpl;
import mb.spoofax.runtime.benchmark.state.InfraState;
import mb.spoofax.runtime.benchmark.state.SpoofaxPieState;
import mb.spoofax.runtime.benchmark.state.WorkspaceState;
import mb.util.async.NullCancelled;
import mb.vfs.path.PPath;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;


@State(Scope.Benchmark)
public class BUTopsortState {
    private BottomUpTopsortExecutor executor;


    public void setup(InfraState infraState) {
        init(infraState);
    }

    public void renew(InfraState infraState) {
        init(infraState);
    }

    private void init(InfraState infraState) {
        this.executor =
            new BottomUpTopsortExecutorImpl(infraState.store, infraState.cache, infraState.share, infraState.layer,
                infraState.logger, infraState.funcs);
    }


    public void addProject(PPath project, SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, Blackhole blackhole) {
        executor.setObserver(project, spoofaxPieState.spoofaxPipeline.project(project, workspaceState.root), obj -> {
            blackhole.consume(obj);
            return Unit.INSTANCE;
        });
        try {
            executor.requireTopDown(spoofaxPieState.spoofaxPipeline.project(project, workspaceState.root),
                new NullCancelled());
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeProject(PPath project) {
        executor.removeObserver(project);
    }

    public void addEditor(String text, PPath file, PPath project, SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, Blackhole blackhole) {
        executor.setObserver(file, spoofaxPieState.spoofaxPipeline.editor(text, file, project, workspaceState.root),
            obj -> {
                blackhole.consume(obj);
                return Unit.INSTANCE;
            });
        try {
            executor.requireTopDown(spoofaxPieState.spoofaxPipeline.editor(text, file, project, workspaceState.root),
                new NullCancelled());
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeEditor(PPath file) {
        executor.removeObserver(file);
    }

    public void pathChanges(List<PPath> changedPaths) {
        try {
            executor.requireBottomUp(changedPaths, new NullCancelled());
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
