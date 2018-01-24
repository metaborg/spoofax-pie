package mb.spoofax.runtime.benchmark.state;

import kotlin.Unit;
import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.FuncApp;
import mb.pie.runtime.core.exec.ObservingExecutor;
import mb.pie.runtime.core.impl.exec.ObservingExecutorImpl;
import mb.spoofax.runtime.pie.builder.SpoofaxPipeline;
import mb.spoofax.runtime.pie.generated.processProject;
import mb.util.async.NullCancelled;
import mb.vfs.path.PPath;
import mb.vfs.path.PPaths;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;


@State(Scope.Benchmark)
public class ObservingExecState {
    public ObservingExecutor executor;

    public void setup(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        init(spoofaxPieState, workspaceState, infraState);
    }

    public void renew(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        init(spoofaxPieState, workspaceState, infraState);
    }

    public void exec(WorkspaceState workspaceState, List<PPath> changedPaths) {
        try {
            executor.requireBottomUp(changedPaths, new NullCancelled());
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void init(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        this.executor =
            new ObservingExecutorImpl(infraState.store, infraState.cache, infraState.share, infraState.layer,
                infraState.logger, spoofaxPieState.logger, infraState.funcs);
        final PPath root = workspaceState.root;
        try(final Stream<PPath> stream = root.list(PPaths.directoryPathMatcher())) {
            for(PPath project : (Iterable<PPath>) stream.filter(
                (path) -> !path.toString().contains("root"))::iterator) {
                final FuncApp<processProject.Input, processProject.Output>
                    app = SpoofaxPipeline.INSTANCE.project(project, root);
                executor.setObserver(project, app, (result) -> Unit.INSTANCE);
                executor.requireTopDown(app, new NullCancelled());
            }
        } catch(ExecException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
