package mb.spoofax.pie.benchmark.state.exec;

import mb.fs.java.JavaFSPath;
import mb.pie.api.ExecException;
import mb.pie.api.Task;
import mb.pie.api.exec.TopDownExecutor;
import mb.pie.api.exec.TopDownSession;
import mb.spoofax.pie.benchmark.state.*;
import mb.spoofax.pie.generated.processContainer;
import mb.spoofax.pie.generated.processDocumentWithText;
import mb.spoofax.pie.processing.ContainerResult;
import mb.spoofax.pie.processing.DocumentResult;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;

@State(Scope.Benchmark)
public class TDState {
    private SpoofaxPieState spoofaxPieState;
    private WorkspaceState workspaceState;
    private TopDownExecutor executor;
    private HashMap<JavaFSPath, Task<processContainer.Input, ContainerResult>> projectTasks = new HashMap<>();
    private HashMap<JavaFSPath, Task<processDocumentWithText.Input, DocumentResult>> editorTasks =
        new HashMap<>();


    public void setup(SpoofaxPieState spoofaxPieState, WorkspaceState workspaceState, InfraState infraState) {
        this.spoofaxPieState = spoofaxPieState;
        this.workspaceState = workspaceState;
        this.executor = infraState.pie.getTopDownExecutor();
    }

    public void reset() {
        this.editorTasks.clear();
    }


    /**
     * Adds or updates a project, and executes a project update.
     */
    public void addOrUpdateProject(JavaFSPath project, Blackhole blackhole) {
        final Task<processContainer.Input, ContainerResult> task =
            spoofaxPieState.spoofaxPipeline.container(project, workspaceState.root);
        projectTasks.put(project, task);

        try {
            final TopDownSession session = executor.newSession();
            blackhole.consume(session.requireInitial(task));
        } catch(ExecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes a project.
     */
    public void removeProject(JavaFSPath project) {
        projectTasks.remove(project);
    }


    /**
     * Adds or updates an editor, and executes an editor update.
     */
    public void addOrUpdateEditor(String text, JavaFSPath file, JavaFSPath project, Blackhole blackhole) {
        final Task<processDocumentWithText.Input, DocumentResult> task =
            spoofaxPieState.spoofaxPipeline.documentWithText(file, project, workspaceState.root, text);
        this.editorTasks.put(file, task);

        try {
            final TopDownSession session = executor.newSession();
            blackhole.consume(session.requireInitial(task));
        } catch(ExecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes an editor.
     */
    public void removeEditor(JavaFSPath file) {
        this.editorTasks.remove(file);
    }


    /**
     * Executes updates for all projects and editors.
     */
    public void execAll(Blackhole blackhole) {
        final TopDownSession session = executor.newSession();
        try {
            for(Task<processContainer.Input, ContainerResult> task : this.projectTasks.values()) {
                blackhole.consume(session.requireInitial(task));
            }
            for(Task<? extends processDocumentWithText.Input, ? extends DocumentResult> task : this.editorTasks.values()) {
                blackhole.consume(session.requireInitial(task));
            }
        } catch(ExecException e) {
            throw new RuntimeException(e);
        }
    }
}
