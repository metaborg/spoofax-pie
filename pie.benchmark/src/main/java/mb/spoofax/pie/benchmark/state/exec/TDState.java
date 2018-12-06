package mb.spoofax.pie.benchmark.state.exec;

import mb.fs.java.JavaFSPath;
import mb.pie.api.ExecException;
import mb.pie.api.Task;
import mb.pie.api.exec.*;
import mb.spoofax.pie.benchmark.state.*;
import mb.spoofax.pie.generated.processDocumentWithText;
import mb.spoofax.pie.processing.DocumentResult;
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
    private Task<JavaFSPath, ? extends Serializable> processWorkspace;
    private HashMap<JavaFSPath, Task<? extends processDocumentWithText.Input, ? extends DocumentResult>> editorTasks = new HashMap<>();


    public void setup(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        this.spoofaxPieState = spoofaxPieState;
        this.workspaceState = workspaceState;
        this.executor = infraState.pie.getTopDownExecutor();
        this.processWorkspace = spoofaxPieState.spoofaxPipeline.workspace(workspaceState.root);
    }

    public void reset() {
        this.editorTasks.clear();
    }


    /**
     * Adds an editor, or updates an exiting editor, and executes an editor update.
     */
    public void addOrUpdateEditor(String text, JavaFSPath file, JavaFSPath project, Blackhole blackhole) {
        final Task<? extends processDocumentWithText.Input, ? extends DocumentResult> task =
            spoofaxPieState.spoofaxPipeline.documentWithText(file, project, workspaceState.root, text);
        this.editorTasks.put(file, task);
        try {
            final TopDownSession session = executor.newSession();
            blackhole.consume(session.requireInitial(task, new NullCancelled()));
        } catch(InterruptedException | ExecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes an open editor.
     */
    public void removeEditor(JavaFSPath file) {
        this.editorTasks.remove(file);
    }

    /**
     * Executes the workspace and all open editors.
     */
    public void execAll(Blackhole blackhole) {
        final TopDownSession session = executor.newSession();
        try {
            final Serializable workspaceResult = session.requireInitial(processWorkspace, new NullCancelled());
            blackhole.consume(workspaceResult);
            for(Task<? extends processDocumentWithText.Input, ? extends DocumentResult> task : this.editorTasks.values()) {
                final Serializable editorResult = session.requireInitial(task, new NullCancelled());
                blackhole.consume(editorResult);
            }
        } catch(ExecException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
