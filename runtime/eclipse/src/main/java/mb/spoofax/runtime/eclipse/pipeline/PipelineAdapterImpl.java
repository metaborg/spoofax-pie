package mb.spoofax.runtime.eclipse.pipeline;

import java.io.Serializable;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.google.inject.Inject;

import kotlin.Unit;
import mb.pie.runtime.core.ObsFuncApp;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.nature.SpoofaxNature;
import mb.spoofax.runtime.eclipse.util.NatureUtils;
import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution;
import mb.spoofax.runtime.model.message.Msg;
import mb.spoofax.runtime.model.style.Styling;
import mb.spoofax.runtime.pie.builder.SpoofaxPipeline;
import mb.vfs.path.PPath;

public class PipelineAdapterImpl implements PipelineAdapterInternal, PipelineAdapter {
    private final EclipsePathSrv pathSrv;
    private final IWorkspaceRoot eclipseWorkspaceRoot;
    private final PPath workspaceRoot;

    private final ArrayList<SpoofaxEditor> editors = new ArrayList<>();
    private final ArrayList<IProject> projects = new ArrayList<>();


    @Inject public PipelineAdapterImpl(EclipsePathSrv pathSrv) {
        this.pathSrv = pathSrv;
        this.eclipseWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        this.workspaceRoot = pathSrv.resolve(eclipseWorkspaceRoot);
    }


    @Override public ArrayList<ObsFuncApp<Serializable, Serializable>> workspaceObsFuncApps(WorkspaceUpdate update) {
        final ArrayList<ObsFuncApp<Serializable, Serializable>> obsFuncApps = new ArrayList<>();
        for(IProject eclipseProject : projects) {
            final PPath project = pathSrv.resolve(eclipseProject);
            obsFuncApps.add(projectObsFuncApp(project, update));
        }
        for(SpoofaxEditor editor : editors) {
            obsFuncApps.add(editorObsFuncApp(editor, update));
        }
        return obsFuncApps;
    }

    @Override public ObsFuncApp<Serializable, Serializable> projectObsFuncApp(PPath project, WorkspaceUpdate update) {
        return SpoofaxPipeline.INSTANCE.processProjectObsFunApp(project, workspaceRoot, (projectResult) -> {
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
            return Unit.INSTANCE;
        });
    }

    @Override public ObsFuncApp<Serializable, Serializable> editorObsFuncApp(String text, PPath file, PPath project,
        SpoofaxEditor editor, WorkspaceUpdate update) {
        return SpoofaxPipeline.INSTANCE.processEditorObsFunApp(text, file, project, workspaceRoot, (output) -> {
            update.addClear(file);
            if(output != null) {
                final ArrayList<Msg> messages = output.component2();
                update.replaceMessages(file, messages);

                final @Nullable Styling styling = output.component3();
                if(styling != null) {
                    update.updateStyle(editor, text, styling);
                } else {
                    update.removeStyle(editor, text.length());
                }

                final @Nullable ConstraintSolverSolution solution = output.component5();
                if(solution != null) {
                    update.addMessages(solution.getFileMessages());
                    update.addMessages(solution.getFileUnsolvedMessages());
                    update.addMessages(project, solution.getProjectMessages());
                    update.addMessages(project, solution.getProjectUnsolvedMessages());
                }
            } else {
                update.removeStyle(editor, text.length());
            }
            return Unit.INSTANCE;
        });
    }

    @Override public ObsFuncApp<Serializable, Serializable> editorObsFuncApp(SpoofaxEditor editor,
        WorkspaceUpdate update) {
        final String text = editor.text();
        final PPath file = editor.file();
        final IProject eclipseProject = editor.eclipseFile().getProject();
        final PPath project = pathSrv.resolve(eclipseProject);
        return editorObsFuncApp(text, file, project, editor, update);
    }


    @Override public void scan() throws CoreException {
        for(IProject project : eclipseWorkspaceRoot.getProjects()) {
            if(project.isAccessible() && NatureUtils.exists(SpoofaxNature.id, project)) {
                addProject(project);
            }
        }
    }


    @Override public void addProject(IProject project) {
        projects.add(project);
    }

    @Override public void removeProject(IProject project) {
        projects.remove(project);
    }


    @Override public void addEditor(SpoofaxEditor editor) {
        editors.add(editor);
    }

    @Override public void removeEditor(SpoofaxEditor editor) {
        editors.remove(editor);
    }
}
