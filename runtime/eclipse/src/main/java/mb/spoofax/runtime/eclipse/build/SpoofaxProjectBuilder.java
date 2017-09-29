package mb.spoofax.runtime.eclipse.build;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.metaborg.meta.nabl2.solver.PartialSolution;

import com.google.inject.Injector;

import mb.log.Logger;
import mb.pie.runtime.builtin.util.Tuple2;
import mb.pie.runtime.builtin.util.Tuple3;
import mb.pie.runtime.builtin.util.Tuple4;
import mb.pie.runtime.builtin.util.Tuple5;
import mb.pie.runtime.core.BuildException;
import mb.pie.runtime.core.BuildManager;
import mb.pie.runtime.core.BuildSession;
import mb.spoofax.runtime.eclipse.SpoofaxPlugin;
import mb.spoofax.runtime.eclipse.editor.Editors;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.util.Nullable;
import mb.spoofax.runtime.eclipse.vfs.EclipsePathSrv;
import mb.spoofax.runtime.impl.nabl.ConstraintSolverSolution;
import mb.spoofax.runtime.model.message.Msg;
import mb.spoofax.runtime.model.parse.Token;
import mb.spoofax.runtime.model.style.Styling;
import mb.spoofax.runtime.pie.PieSrv;
import mb.spoofax.runtime.pie.generated.processString;
import mb.spoofax.runtime.pie.generated.processWorkspace;
import mb.vfs.path.PPath;

public class SpoofaxProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = SpoofaxPlugin.id + ".builder";

    private final Logger logger;
    private final EclipsePathSrv pathSrv;
    private final PieSrv pieSrv;
    private final Editors editors;
    private final WorkspaceUpdateFactory workspaceUpdateFactory;
    
    private final IWorkspaceRoot eclipseWorkspaceRoot;
    private final PPath workspaceRoot;


    public SpoofaxProjectBuilder() {
        final Injector injector = SpoofaxPlugin.spoofaxFacade().injector;
        this.logger = injector.getInstance(Logger.class).forContext(getClass());
        this.pathSrv = injector.getInstance(EclipsePathSrv.class);
        this.pieSrv = injector.getInstance(PieSrv.class);
        this.editors = injector.getInstance(Editors.class);
        this.workspaceUpdateFactory = injector.getInstance(WorkspaceUpdateFactory.class);
        
        this.eclipseWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        this.workspaceRoot = pathSrv.resolveWorkspaceRoot();
    }


    @Override protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
        throws CoreException {
        final IProject currentEclipseProject = getProject();
        if(kind == AUTO_BUILD || kind == INCREMENTAL_BUILD) {
            if(!hasRelevantChanges(getDelta(currentEclipseProject))) {
                return null;
            }
        }

        logger.info("Building workspace, requested from project {}", currentEclipseProject);
        final BuildManager buildManager = pieSrv.get(workspaceRoot);
        final BuildSession buildSession = buildManager.newSession();

        // @formatter:off
        ArrayList<
            ? extends Tuple3<
                // Project path
                ? extends PPath,
                // Language specification results
                ? extends ArrayList<? extends Tuple2<
                    // Per-file results
                    ? extends ArrayList<? extends Tuple5<
                        ? extends PPath, 
                        ? extends ArrayList<@Nullable Token>, 
                        ? extends ArrayList<Msg>, 
                        @Nullable ? extends Styling, 
                        @Nullable ? extends PartialSolution
                    >>,
                    // Constraint solver solution
                    @Nullable ? extends ConstraintSolverSolution
                >>, 
                // Spoofax Core per-file results
                ? extends ArrayList<? extends Tuple4<
                    ? extends PPath, 
                    ? extends ArrayList<@Nullable Token>, 
                    ? extends ArrayList<Msg>, 
                    @Nullable ? extends Styling
                >>
            >
        > workspaceResults;
        // @formatter:on

        try {
            workspaceResults = buildSession.build(processWorkspace.class, workspaceRoot);
        } catch(BuildException e) {
            workspaceResults = new ArrayList<>();
            logger.error("Could not build workspace, requested from project {}", e, currentEclipseProject);
        }

        final WorkspaceUpdate update = workspaceUpdateFactory.create();
        update.addClearRec(workspaceRoot);

        workspaceResults.stream().forEach((projectResult) -> {
            final PPath project = projectResult.component1();
            projectResult.component2().stream().forEach((result) -> {
                result.component1().stream().forEach((fileResult) -> {
                    final PPath file = fileResult.component1();
                    final ArrayList<Msg> messages = fileResult.component3();
                    update.addMessages(file, messages);
                });
                final @Nullable ConstraintSolverSolution solution = result.component2();
                if(solution != null) {
                    update.addMessages(solution.getFileMessages());
                    update.addMessages(solution.getFileUnsolvedMessages());
                    update.addMessages(project, solution.getProjectMessages());
                    update.addMessages(project, solution.getProjectUnsolvedMessages());
                }
            });
            projectResult.component3().stream().forEach((result) -> {
                final PPath file = result.component1();
                final ArrayList<Msg> messages = result.component3();
                update.addMessages(file, messages);
            });
        });

        for(SpoofaxEditor editor : editors.editors()) {
            final String name = editor.name();
            final String text = editor.text();
            final PPath file = editor.file();
            update.addClear(file);
            
            final IProject eclipseProject = editor.eclipseFile().getProject();
            final PPath project = pathSrv.resolve(eclipseProject);

            try {
                final processString.Output output = buildSession.build(processString.class,
                    new processString.Input(text, file, project, workspaceRoot));
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
            } catch(BuildException e) {
                logger.error("Could not update editor {}", e, name);
            }
        }

        update.updateMessagesSync(eclipseWorkspaceRoot, monitor);
        update.updateStyleAsync(monitor);

        return null;
    }

    @Override protected void clean(IProgressMonitor monitor) throws CoreException {
        final BuildManager buildManager = pieSrv.get(workspaceRoot);
        buildManager.dropStore();
        buildManager.dropCache();

        final WorkspaceUpdate update = workspaceUpdateFactory.create();
        update.addClearRec(workspaceRoot);
        update.updateMessagesSync(eclipseWorkspaceRoot, monitor);

        forgetLastBuiltState();
    }

    @Override public ISchedulingRule getRule(int kind, Map<String, String> args) {
        return eclipseWorkspaceRoot;
    }


    private final boolean hasRelevantChanges(IResourceDelta delta) throws CoreException {
        final AtomicBoolean relevant = new AtomicBoolean(false);
        delta.accept(new IResourceDeltaVisitor() {
            @Override public boolean visit(IResourceDelta innerDelta) throws CoreException {
                switch(innerDelta.getKind()) {
                    case IResourceDelta.ADDED_PHANTOM:
                    case IResourceDelta.REMOVED_PHANTOM:
                        return false;
                }
                relevant.set(true);
                final IResource resource = innerDelta.getResource();
                if(resource.getType() == IResource.FILE) {
                    final String extension = resource.getFileExtension();
                    if(extension == null) {
                        relevant.set(true);
                    } else if(!"mdb".equals(extension)) {
                        relevant.set(true);
                    }
                }
                return true;
            }
        });
        return relevant.get();
    }
}
