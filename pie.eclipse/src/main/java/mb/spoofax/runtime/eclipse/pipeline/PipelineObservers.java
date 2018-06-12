package mb.spoofax.runtime.eclipse.pipeline;

import com.google.inject.Inject;
import java.util.ArrayList;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import mb.pie.vfs.path.PPath;
import mb.spoofax.api.message.Msg;
import mb.spoofax.api.style.Styling;
import mb.spoofax.pie.generated.processEditor;
import mb.spoofax.pie.generated.processProject;
import mb.spoofax.runtime.constraint.CSolution;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.util.Nullable;

public class PipelineObservers {
    private final WorkspaceUpdateFactory workspaceUpdateFactory;


    @Inject public PipelineObservers(WorkspaceUpdateFactory workspaceUpdateFactory) {
        this.workspaceUpdateFactory = workspaceUpdateFactory;
    }


    private final class ProjectBuildObserver implements Function1<processProject.Output, Unit> {
        private final PPath project;

        private ProjectBuildObserver(PPath project) {
            this.project = project;
        }

        @Override public Unit invoke(processProject.Output projectResult) {
            final WorkspaceUpdate update = workspaceUpdateFactory.create();
            update.addClearRec(project);
            if(projectResult != null) {
                projectResult.component1().stream().flatMap((r) -> r.stream()).forEach((fileResult) -> {
                    final PPath file = fileResult.component1();
                    final ArrayList<Msg> messages = fileResult.component3();
                    update.addMessages(file, messages);
                    final @Nullable CSolution solution = fileResult.component5();
                    if(solution != null) {
                        update.addMessages(solution.getFileMessages());
                        update.addMessages(project, solution.getProjectMessages());
                    }
                });
                projectResult.component2().stream().forEach((result) -> {
                    final PPath file = result.component1();
                    final ArrayList<Msg> messages = result.component3();
                    update.addMessages(file, messages);
                });
            }

            update.update(WorkspaceUpdate.lock, null);

            return Unit.INSTANCE;
        }

        @Override public String toString() {
            return "ProjectBuildObserver(" + project + ")";
        }
    }

    public Function1<? super processProject.Output, Unit> project(PPath project) {
        return new ProjectBuildObserver(project);
    }


    private final class EditorObserver implements Function1<processEditor.Output, Unit> {
        private final SpoofaxEditor editor;
        private final String text;
        private final PPath file;
        private final PPath project;

        private EditorObserver(SpoofaxEditor editor, String text, PPath file, PPath project) {
            this.project = project;
            this.editor = editor;
            this.text = text;
            this.file = file;
        }

        @Override public Unit invoke(processEditor.Output editorResult) {
            final WorkspaceUpdate update = workspaceUpdateFactory.create();
            update.addClear(file);
            if(editorResult != null) {
                final ArrayList<Msg> messages = editorResult.component2();
                update.replaceMessages(file, messages);

                final @Nullable Styling styling = editorResult.component3();
                if(styling != null) {
                    update.updateStyle(editor, text, styling);
                } else {
                    update.removeStyle(editor, text.length());
                }

                final @Nullable CSolution solution = editorResult.component4();
                if(solution != null) {
                    update.addMessagesFiltered(solution.getFileMessages(), file);
                }
            } else {
                update.removeStyle(editor, text.length());
            }

            // TODO: pass in file as scheduling rule?
            update.update(WorkspaceUpdate.lock, null);

            return Unit.INSTANCE;
        }

        @Override public String toString() {
            return "EditorObserver(" + editor + ", " + text.substring(0, Math.max(0, Math.min(100, text.length() - 1)))
                + ", " + file + ", " + project + ")";
        }
    }

    public Function1<? super processEditor.Output, Unit> editor(SpoofaxEditor editor, String text, PPath file,
        PPath project) {
        return new EditorObserver(editor, text, file, project);
    }
}
