package mb.spoofax.runtime.eclipse.pipeline;

import com.google.inject.Inject;
import java.util.ArrayList;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import mb.fs.java.JavaFSPath;
import mb.spoofax.api.message.Message;
import mb.spoofax.api.style.Styling;
import mb.spoofax.pie.processing.ContainerResult;
import mb.spoofax.pie.processing.DocumentResult;
import mb.spoofax.runtime.analysis.Analyzer.FinalOutput;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.util.Nullable;

public class PipelineObservers {
    private final WorkspaceUpdateFactory workspaceUpdateFactory;


    @Inject public PipelineObservers(WorkspaceUpdateFactory workspaceUpdateFactory) {
        this.workspaceUpdateFactory = workspaceUpdateFactory;
    }


    private final class ContainerBuildObserver implements Function1<ContainerResult, Unit> {
        private final JavaFSPath container;

        private ContainerBuildObserver(JavaFSPath container) {
            this.container = container;
        }

        @Override public Unit invoke(ContainerResult containerResult) {
            final WorkspaceUpdate update = workspaceUpdateFactory.create();
            update.addClearRec(container);
            if(containerResult != null) {
                containerResult.getDocumentResults().forEach((documentResult) -> {
                    final JavaFSPath document = documentResult.getDocument();
                    final ArrayList<Message> messages = documentResult.getMessages();
                    update.addMessages(messages, document);
                    final @Nullable FinalOutput analysis = documentResult.getAnalysis();
                    if(analysis != null) {
                        update.addMessages(analysis.messages);
                    }
                });
            }
            update.update(WorkspaceUpdate.lock, null);
            return Unit.INSTANCE;
        }

        @Override public String toString() {
            return "ContainerBuildObserver(" + container + ")";
        }
    }

    public Function1<? super ContainerResult, Unit> container(JavaFSPath container) {
        return new ContainerBuildObserver(container);
    }


    private final class EditorObserver implements Function1<DocumentResult, Unit> {
        private final SpoofaxEditor editor;
        private final String text;
        private final JavaFSPath document;
        private final JavaFSPath container;

        private EditorObserver(SpoofaxEditor editor, String text, JavaFSPath document, JavaFSPath container) {
            this.editor = editor;
            this.text = text;
            this.document = document;
            this.container = container;
        }

        @Override public Unit invoke(DocumentResult documentResult) {
            final WorkspaceUpdate update = workspaceUpdateFactory.create();
            update.addClear(document);
            if(documentResult != null) {
                final ArrayList<Message> messages = documentResult.getMessages();
                update.replaceMessages(messages, document);

                final @Nullable Styling styling = documentResult.getStyling();
                if(styling != null) {
                    update.updateStyle(editor, text, styling);
                } else {
                    update.removeStyle(editor, text.length());
                }

                final @Nullable FinalOutput analysis = documentResult.getAnalysis();
                if(analysis != null) {
                    update.addMessages(analysis.messages);
                }
            } else {
                update.removeStyle(editor, text.length());
            }
            // TODO: pass in document as scheduling rule?
            update.update(WorkspaceUpdate.lock, null);
            return Unit.INSTANCE;
        }

        @Override public String toString() {
            return "EditorObserver(" + editor + ", " + text.substring(0, Math.max(0, Math.min(100, text.length() - 1)))
                + ", " + document + ", " + container + ")";
        }
    }

    public Function1<? super DocumentResult, Unit> editor(SpoofaxEditor editor, String text, JavaFSPath file,
        JavaFSPath project) {
        return new EditorObserver(editor, text, file, project);
    }
}
