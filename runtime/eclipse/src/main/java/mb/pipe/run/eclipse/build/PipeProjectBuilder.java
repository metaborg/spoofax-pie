package mb.pipe.run.eclipse.build;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.google.inject.Injector;

import mb.log.Logger;
import mb.pie.runtime.builtin.util.Tuple4;
import mb.pie.runtime.core.BuildException;
import mb.pie.runtime.core.BuildManager;
import mb.pie.runtime.core.BuildSession;
import mb.pipe.run.eclipse.PipePlugin;
import mb.pipe.run.eclipse.editor.Editors;
import mb.pipe.run.eclipse.editor.PipeEditor;
import mb.pipe.run.eclipse.util.MarkerUtils;
import mb.pipe.run.eclipse.util.Nullable;
import mb.pipe.run.eclipse.vfs.EclipsePathSrv;
import mb.spoofax.runtime.model.message.Msg;
import mb.spoofax.runtime.model.parse.Token;
import mb.spoofax.runtime.model.style.Styling;
import mb.spoofax.runtime.pie.PieSrv;
import mb.spoofax.runtime.pie.generated.processProject;
import mb.spoofax.runtime.pie.generated.processString;
import mb.vfs.path.PPath;

public class PipeProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = PipePlugin.id + ".builder";

    private final Logger logger;
    private final EclipsePathSrv pathSrv;
    private final PieSrv pieSrv;
    private final Editors editors;
    private final Updater updater;


    public PipeProjectBuilder() {
        final Injector injector = PipePlugin.spoofaxFacade().injector;
        this.logger = injector.getInstance(Logger.class).forContext(getClass());
        this.pathSrv = injector.getInstance(EclipsePathSrv.class);
        this.pieSrv = injector.getInstance(PieSrv.class);
        this.editors = injector.getInstance(Editors.class);
        this.updater = injector.getInstance(Updater.class);
    }


    @Override protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
        throws CoreException {
        if(kind == AUTO_BUILD || kind == INCREMENTAL_BUILD) {
            if(!hasRelevantChanges(getDelta(getProject()))) {
                return null;
            }
        }

        final IProject project = getProject();
        final PPath projectPath = pathSrv.resolve(project);
        final PPath workspaceRoot = pathSrv.resolveWorkspaceRoot();

        logger.info("Building project {}", project);
        final BuildManager buildManager = pieSrv.get(workspaceRoot);
        final BuildSession buildSession = buildManager.newSession();
        ArrayList<Tuple4<? extends PPath, @Nullable ? extends ArrayList<Token>, ? extends ArrayList<Msg>, @Nullable ? extends Styling>> results;
        try {
            results = buildSession.build(processProject.class, new processProject.Input(workspaceRoot, projectPath));
        } catch(BuildException e) {
            results = new ArrayList<>();
            logger.error("Could build workspace project {}", e, project);
        }

        for(Tuple4<? extends PPath, @Nullable ? extends ArrayList<Token>, ? extends ArrayList<Msg>, @Nullable ? extends Styling> result : results) {
            final PPath file = result.component1();
            logger.info("Updating file {}", file);
            final IResource eclipseFile = pathSrv.unresolve(file);
            final List<Msg> messages = result.component3();
            updater.updateMessagesSync(eclipseFile, messages, monitor);
        }

        for(PipeEditor editor : editors.editors()) {
            final IProject editorProject = editor.eclipseFile().getProject();
            if(!project.equals(editorProject)) {
                continue;
            }

            final String name = editor.name();
            final String text = editor.text();
            final PPath associatedFile = editor.file();
            logger.info("Updating editor {}", name);

            try {
                final processString.Output output = buildSession.build(processString.class,
                    new processString.Input(text, associatedFile, projectPath, workspaceRoot));

                final IResource eclipseFile = editor.eclipseFile();
                if(output != null) {
                    final List<Msg> messages = output.component2();
                    updater.updateMessagesSync(eclipseFile, messages, monitor);

                    final @Nullable Styling styling = output.component3();
                    if(styling != null) {
                        updater.updateStyleAsync(editor, text, styling, monitor);
                    } else {
                        updater.removeStyleAsync(editor, text.length(), monitor);
                    }
                } else {
                    updater.clearMessagesSync(eclipseFile, monitor);
                    updater.removeStyleAsync(editor, text.length(), monitor);
                }
            } catch(BuildException e) {
                logger.error("Could not update editor {}", e, name);
            }
        }

        // Trigger referencing projects
        final WorkspaceJob job = new ReferencingProjectBuilder(project, logger);
        job.schedule();
        return project.getReferencedProjects();
    }

    @Override protected void clean(IProgressMonitor monitor) throws CoreException {
        final IProject project = getProject();
        MarkerUtils.clearAllRec(project);

        final PPath workspaceRoot = pathSrv.resolveWorkspaceRoot();
        final BuildManager buildManager = pieSrv.get(workspaceRoot);
        buildManager.dropStore();
        buildManager.dropCache();

        forgetLastBuiltState();
    }

    @Override public ISchedulingRule getRule(int kind, Map<String, String> args) {
        return null;
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
