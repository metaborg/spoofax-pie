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
    private SpoofaxPieState spoofaxPieState;
    private WorkspaceState workspaceState;
    private BottomUpTopsortExecutor executor;


    public void setup(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        this.spoofaxPieState = spoofaxPieState;
        this.workspaceState = workspaceState;
        this.executor =
            new BottomUpTopsortExecutorImpl(infraState.store, infraState.cache, infraState.share, infraState.layer,
                infraState.logger, infraState.funcs);
    }


    /**
     * Adds a project, or updates a project, by setting the observer and then executing a project update in a top-down manner.
     */
    public void addProject(PPath project, Blackhole blackhole) {
        executor.setObserver(project, spoofaxPieState.spoofaxPipeline.project(project, workspaceState.root), obj -> {
            blackhole.consume(obj);
            return Unit.INSTANCE;
        });
        try {
            blackhole.consume(
                executor.requireTopDown(spoofaxPieState.spoofaxPipeline.project(project, workspaceState.root),
                    new NullCancelled()));
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes a project, removing the observer for that project.
     */
    public void removeProject(PPath project) {
        executor.removeObserver(project);
    }


    /**
     * Adds an editor, or updates an existing editor, by setting the observer and then executing an editor update in a top-down manner.
     */
    public void addOrUpdateEditor(String text, PPath file, PPath project, Blackhole blackhole) {
        executor.setObserver(file, spoofaxPieState.spoofaxPipeline.editor(text, file, project, workspaceState.root),
            obj -> {
                blackhole.consume(obj);
                return Unit.INSTANCE;
            });
        try {
            blackhole.consume(executor.requireTopDown(
                spoofaxPieState.spoofaxPipeline.editor(text, file, project, workspaceState.root),
                new NullCancelled()));
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes an open editor, removing the observer for that editor.
     */
    public void removeEditor(PPath file) {
        executor.removeObserver(file);
    }


    /**
     * Executes the pipeline in a bottom-up way, with given changed paths.
     */
    public void execPathChanges(List<PPath> changedPaths) {
        try {
            executor.requireBottomUp(changedPaths, new NullCancelled());
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
