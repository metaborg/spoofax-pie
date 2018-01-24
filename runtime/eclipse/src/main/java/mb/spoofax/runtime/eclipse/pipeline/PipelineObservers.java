package mb.spoofax.runtime.eclipse.pipeline;

import java.util.ArrayList;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.google.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import mb.log.Logger;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution;
import mb.spoofax.runtime.model.message.Msg;
import mb.spoofax.runtime.model.style.Styling;
import mb.spoofax.runtime.pie.generated.processEditor;
import mb.spoofax.runtime.pie.generated.processProject;
import mb.vfs.path.PPath;

public class PipelineObservers {
    private final Logger logger;
    private final WorkspaceUpdateFactory workspaceUpdateFactory;
    private final IWorkspaceRoot eclipseWorkspaceRoot;


    @Inject public PipelineObservers(Logger logger, WorkspaceUpdateFactory workspaceUpdateFactory) {
        this.logger = logger;
        this.workspaceUpdateFactory = workspaceUpdateFactory;
        this.eclipseWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    }


    public Function1<? super processProject.Output, Unit> project(PPath project) {
        return (projectResult) -> {
            final WorkspaceUpdate update = workspaceUpdateFactory.create();
            update.addClearRec(project);
            if(projectResult != null) {
                projectResult.component1().stream().forEach((result) -> {
                    result.component1().stream().forEach((fileResult) -> {
                        final PPath file = fileResult.component1();
                        final ArrayList<Msg> messages = fileResult.component3();
                        update.addMessages(file, messages);
                    });
                    final ArrayList<@Nullable ? extends ConstraintSolverSolution> solutions = result.component2();
                    for(@Nullable ConstraintSolverSolution solution : solutions) {
                        if(solution != null) {
                            update.addMessages(solution.getFileMessages());
                            update.addMessages(solution.getFileUnsolvedMessages());
                            update.addMessages(project, solution.getProjectMessages());
                            update.addMessages(project, solution.getProjectUnsolvedMessages());
                        }
                    }
                });
                projectResult.component2().stream().forEach((result) -> {
                    final PPath file = result.component1();
                    final ArrayList<Msg> messages = result.component3();
                    update.addMessages(file, messages);
                });
            }
            try {
                update.updateMessagesSync(eclipseWorkspaceRoot, null);
            } catch(CoreException e) {
                logger.error("Failed to update messages for project {}", e, project);
            }
            update.updateStyleAsync(null);
            return Unit.INSTANCE;
        };
    }

    public Function1<? super processEditor.Output, Unit> editor(SpoofaxEditor editor, String text, PPath file,
        PPath project) {
        return (editorResult) -> {
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

                final @Nullable ConstraintSolverSolution solution = editorResult.component5();
                if(solution != null) {
                    update.addMessagesFiltered(solution.getFileMessages(), file);
                    update.addMessagesFiltered(solution.getFileUnsolvedMessages(), file);
                }
            } else {
                update.removeStyle(editor, text.length());
            }
            try {
                update.updateMessagesSync(null, null);
            } catch(CoreException e) {
                logger.error("Failed to update messages for project {}", e, project);
            }
            update.updateStyleAsync(null);
            return Unit.INSTANCE;
        };
    }
}
