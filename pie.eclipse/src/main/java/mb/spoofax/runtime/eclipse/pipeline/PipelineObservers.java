package mb.spoofax.runtime.eclipse.pipeline;

import com.google.inject.Inject;
import java.util.ArrayList;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import mb.pie.vfs.path.PPath;
import mb.spoofax.api.message.Msg;
import mb.spoofax.api.style.Styling;
import mb.spoofax.pie.processing.DocumentResult;
import mb.spoofax.pie.processing.ProjectResult;
import mb.spoofax.runtime.constraint.CSolution;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.util.Nullable;

public class PipelineObservers {
    private final WorkspaceUpdateFactory workspaceUpdateFactory;


    @Inject public PipelineObservers(WorkspaceUpdateFactory workspaceUpdateFactory) {
        this.workspaceUpdateFactory = workspaceUpdateFactory;
    }


    private final class ProjectBuildObserver implements Function1<ProjectResult, Unit> {
        private final PPath project;

        private ProjectBuildObserver(PPath project) {
            this.project = project;
        }

        @Override public Unit invoke(ProjectResult projectResult) {
            final WorkspaceUpdate update = workspaceUpdateFactory.create();
            update.addClearRec(project);
            if(projectResult != null) {
                projectResult.getDocumentResults().forEach((documentResult) -> {
                    final PPath file = documentResult.getDocument();
                    final ArrayList<Msg> messages = documentResult.getMessages();
                    update.addMessages(file, messages);
                    final @Nullable CSolution constraintsSolution = documentResult.getConstraintsSolution();
                    if(constraintsSolution != null) {
                        update.addMessages(constraintsSolution.getFileMessages());
                        update.addMessages(project, constraintsSolution.getProjectMessages());
                    }
                });
            }
            update.update(WorkspaceUpdate.lock, null);
            return Unit.INSTANCE;
        }

        @Override public String toString() {
            return "ProjectBuildObserver(" + project + ")";
        }
    }

    public Function1<? super ProjectResult, Unit> project(PPath project) {
        return new ProjectBuildObserver(project);
    }


    private final class EditorObserver implements Function1<DocumentResult, Unit> {
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

        @Override public Unit invoke(DocumentResult documentResult) {
            final WorkspaceUpdate update = workspaceUpdateFactory.create();
            update.addClear(file);
            if(documentResult != null) {
                final ArrayList<Msg> messages = documentResult.getMessages();
                update.replaceMessages(file, messages);

                final @Nullable Styling styling = documentResult.getStyling();
                if(styling != null) {
                    update.updateStyle(editor, text, styling);
                } else {
                    update.removeStyle(editor, text.length());
                }

                final @Nullable CSolution constraintsSolution = documentResult.getConstraintsSolution();
                if(constraintsSolution != null) {
                    update.addMessagesFiltered(constraintsSolution.getFileMessages(), file);
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

    public Function1<? super DocumentResult, Unit> editor(SpoofaxEditor editor, String text, PPath file,
        PPath project) {
        return new EditorObserver(editor, text, file, project);
    }
}
