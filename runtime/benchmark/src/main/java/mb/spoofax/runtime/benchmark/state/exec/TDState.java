package mb.spoofax.runtime.benchmark.state.exec;

import mb.pie.runtime.core.ExecException;
import mb.pie.runtime.core.FuncApp;
import mb.pie.runtime.core.exec.TopDownExec;
import mb.pie.runtime.core.impl.exec.TopDownExecImpl;
import mb.spoofax.runtime.benchmark.state.InfraState;
import mb.spoofax.runtime.benchmark.state.SpoofaxPieState;
import mb.spoofax.runtime.benchmark.state.WorkspaceState;
import mb.spoofax.runtime.pie.generated.processEditor;
import mb.util.async.NullCancelled;
import mb.vfs.path.PPath;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.Serializable;
import java.util.HashMap;


@State(Scope.Benchmark)
public class TDState {
    private SpoofaxPieState spoofaxPieState;
    private WorkspaceState workspaceState;
    private TopDownExec exec;
    private FuncApp<PPath, ? extends Serializable> processWorkspace;
    private HashMap<PPath, FuncApp<? extends processEditor.Input, ? extends processEditor.Output>> editors =
        new HashMap<>();


    public void setup(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        this.spoofaxPieState = spoofaxPieState;
        this.workspaceState = workspaceState;
        this.exec =
            new TopDownExecImpl(infraState.store, infraState.cache, infraState.share, infraState.layer.get(),
                infraState.logger.get(), infraState.funcs);
        this.processWorkspace = spoofaxPieState.spoofaxPipeline.workspace(workspaceState.root);
    }


    /**
     * Adds an editor, or updates an exiting editor, and executes an editor update.
     */
    public void addOrUpdateEditor(String text, PPath file, PPath project, Blackhole blackhole) {
        final FuncApp<? extends processEditor.Input, ? extends processEditor.Output> app =
            spoofaxPieState.spoofaxPipeline.editor(text, file, project, workspaceState.root);
        this.editors.put(file, app);
        try {
            blackhole.consume(exec.requireOutput(app, new NullCancelled()));
        } catch(InterruptedException | ExecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes an open editor.
     */
    public void removeEditor(PPath file) {
        this.editors.remove(file);
    }

    /**
     * Executes the workspace and all open editors.
     */
    public void execAll(Blackhole blackhole) {
        try {
            final Serializable workspaceResult = exec.requireOutput(processWorkspace, new NullCancelled());
            blackhole.consume(workspaceResult);
            for(FuncApp<? extends processEditor.Input, ? extends processEditor.Output> editor : this.editors.values()) {
                final Serializable editorResult = exec.requireOutput(editor, new NullCancelled());
                blackhole.consume(editorResult);
            }
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
