package mb.spoofax.runtime.benchmark.state.exec;

import mb.pie.api.ExecException;
import mb.pie.api.Task;
import mb.pie.api.exec.*;
import mb.pie.vfs.path.PPath;
import mb.spoofax.runtime.benchmark.state.*;
import mb.spoofax.runtime.pie.generated.processEditor;
import mb.spoofax.runtime.pie.generated.processEditor.Input;
import mb.spoofax.runtime.pie.generated.processEditor.Output;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.Serializable;
import java.util.HashMap;


@State(Scope.Benchmark)
public class TDState {
    private SpoofaxPieState spoofaxPieState;
    private WorkspaceState workspaceState;
    private TopDownExecutor executor;
    private Task<PPath, ? extends Serializable> processWorkspace;
    private HashMap<PPath, Task<? extends processEditor.Input, ? extends processEditor.Output>> editors =
        new HashMap<>();


    public void setup(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        this.spoofaxPieState = spoofaxPieState;
        this.workspaceState = workspaceState;
        this.executor = infraState.pie.getTopDownExecutor();
        this.processWorkspace = spoofaxPieState.spoofaxPipeline.workspace(workspaceState.root);
    }


    /**
     * Adds an editor, or updates an exiting editor, and executes an editor update.
     */
    public void addOrUpdateEditor(String text, PPath file, PPath project, Blackhole blackhole) {
        final Task<? extends Input, ? extends Output> app =
            spoofaxPieState.spoofaxPipeline.editor(text, file, project, workspaceState.root);
        this.editors.put(file, app);
        try {
            final TopDownSession session = executor.newSession();
            blackhole.consume(session.requireInitial(app, new NullCancelled()));
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
        final TopDownSession session = executor.newSession();
        try {
            final Serializable workspaceResult = session.requireInitial(processWorkspace, new NullCancelled());
            blackhole.consume(workspaceResult);
            for(Task<? extends processEditor.Input, ? extends processEditor.Output> editor : this.editors.values()) {
                final Serializable editorResult = session.requireInitial(editor, new NullCancelled());
                blackhole.consume(editorResult);
            }
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
