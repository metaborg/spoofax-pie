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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.google.inject.Injector;

import mb.ceres.BuildException;
import mb.ceres.BuildManager;
import mb.pipe.run.ceres.CeresSrv;
import mb.pipe.run.ceres.generated.processProject;
import mb.pipe.run.ceres.generated.processString;
import mb.pipe.run.ceres.util.Tuple4;
import mb.pipe.run.core.log.Logger;
import mb.pipe.run.core.model.Context;
import mb.pipe.run.core.model.ContextFactory;
import mb.pipe.run.core.model.message.Msg;
import mb.pipe.run.core.model.parse.Token;
import mb.pipe.run.core.model.style.Styling;
import mb.pipe.run.core.path.PPath;
import mb.pipe.run.eclipse.PipePlugin;
import mb.pipe.run.eclipse.editor.Editors;
import mb.pipe.run.eclipse.editor.PipeEditor;
import mb.pipe.run.eclipse.util.MarkerUtils;
import mb.pipe.run.eclipse.util.Nullable;
import mb.pipe.run.eclipse.vfs.EclipsePathSrv;

public class PipeLangSpecProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = PipePlugin.id + ".builder.langspec";

    private final Logger logger;
    private final EclipsePathSrv pathSrv;
    private final ContextFactory contextFactory;
    private final CeresSrv ceresSrv;
    private final Editors editors;
    private final Updater updater;


    public PipeLangSpecProjectBuilder() {
        final Injector injector = PipePlugin.pipeFacade().injector;
        this.logger = injector.getInstance(Logger.class).forContext(getClass());
        this.pathSrv = injector.getInstance(EclipsePathSrv.class);
        this.contextFactory = injector.getInstance(ContextFactory.class);
        this.ceresSrv = injector.getInstance(CeresSrv.class);
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
        final PPath workspaceRoot = pathSrv.resolveWorkspaceRoot();
        final PPath projectDir = pathSrv.resolve(project);
        final Context context = contextFactory.create(projectDir);

        logger.info("Building project {}", project);
        final BuildManager buildManager = ceresSrv.get(workspaceRoot);
        ArrayList<Tuple4<? extends PPath, @Nullable ? extends ArrayList<Token>, ? extends ArrayList<Msg>, @Nullable ? extends Styling>> results;
        try {
            results = buildManager.build(processProject.class, new processProject.Input(context, workspaceRoot));
        } catch(BuildException e) {
            results = new ArrayList<>();
            logger.error("Could not build project {}", e, project);
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
                final processString.Output output = buildManager.build(processString.class,
                    new processString.Input(text, associatedFile, context, workspaceRoot));

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

        return null;
    }

    @Override protected void clean(IProgressMonitor monitor) throws CoreException {
        final IProject project = getProject();
        MarkerUtils.clearAllRec(project);

        final PPath workspaceRoot = pathSrv.resolveWorkspaceRoot();
        final BuildManager buildManager = ceresSrv.get(workspaceRoot);
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
                    case IResourceDelta.REMOVED:
                    case IResourceDelta.ADDED_PHANTOM:
                    case IResourceDelta.REMOVED_PHANTOM:
                        return false;
                }
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
