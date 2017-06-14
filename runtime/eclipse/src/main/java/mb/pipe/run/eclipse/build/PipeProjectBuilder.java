package mb.pipe.run.eclipse.build;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.inject.Injector;

import mb.ceres.BuildException;
import mb.ceres.BuildManager;
import mb.pipe.run.ceres.CeresSrv;
import mb.pipe.run.ceres.generated.processFile;
import mb.pipe.run.ceres.generated.processString;
import mb.pipe.run.core.log.Logger;
import mb.pipe.run.core.model.Context;
import mb.pipe.run.core.model.ContextFactory;
import mb.pipe.run.core.model.message.Msg;
import mb.pipe.run.core.model.style.Styling;
import mb.pipe.run.core.path.PPath;
import mb.pipe.run.eclipse.PipePlugin;
import mb.pipe.run.eclipse.editor.Editors;
import mb.pipe.run.eclipse.editor.PipeEditor;
import mb.pipe.run.eclipse.util.Nullable;
import mb.pipe.run.eclipse.util.StatusUtils;
import mb.pipe.run.eclipse.vfs.EclipsePathSrv;

public class PipeProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = PipePlugin.id + ".builder";

    private final Logger logger;
    private final EclipsePathSrv pathSrv;
    private final ContextFactory contextFactory;
    private final CeresSrv ceresSrv;
    private final Editors editors;
    private final Updater updater;


    public PipeProjectBuilder() {
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
        final IProject project = getProject();
        final PPath projectDir = pathSrv.resolve(project);
        final Context context = contextFactory.create(projectDir);

        final List<PPath> minFiles;
        final PathMatcher minFileMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.min");
        final PathMatcher relevanceMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.{min,esv,sdf3}");
        try {
            minFiles = allFiles(projectDir, minFileMatcher);
            if(kind != FULL_BUILD) {
                final IResourceDelta delta = getDelta(project);
                if(delta != null) {
                    final List<PPath> changedRelevantFiles = deltaFiles(delta, relevanceMatcher);
                    if(changedRelevantFiles.isEmpty()) {
                        return null;
                    }
                }
            }
        } catch(IOException e) {
            final String message = "Could not list relevant files to update";
            logger.error(message, e);
            throw new CoreException(StatusUtils.error(message, e));
        }

        if(minFiles.isEmpty()) {
            return null;
        }

        logger.info("Building project " + project);
        final BuildManager buildManager = ceresSrv.get(context);
        for(PPath file : minFiles) {
            logger.info("Updating file {}", file);
            try {
                final processFile.Output output =
                    buildManager.build(processFile.class, new processFile.Input(file, context));

                final List<Msg> messages = output.component5();
                updater.updateMessagesSync(project, messages, monitor);
            } catch(BuildException e) {
                logger.error("Could not update file {}", e, file);
            }
        }
        for(PipeEditor editor : editors.editors()) {
            final String name = editor.name();
            logger.info("Updating editor {}", name);
            final String text = editor.text();
            try {
                final processString.Output output =
                    buildManager.build(processString.class, new processString.Input(text, context));

                final List<Msg> messages = output.component4();
                updater.updateMessagesSync(editor.eclipseResource(), messages, monitor);

                final @Nullable Styling styling = output.component5();
                if(styling != null) {
                    updater.updateStyle(editor.sourceViewer(), styling, monitor);
                }
            } catch(BuildException e) {
                logger.error("Could not update editor {}", e, name);
            }
        }

        return null;
    }

    private final List<PPath> allFiles(PPath dir, PathMatcher matcher) throws IOException {
        // @formatter:off
        return Files
            .walk(dir.getJavaPath())
            .filter((path) -> matcher.matches(path))
            .map((path) -> pathSrv.resolve(path))
            .collect(Collectors.toList());
        // @formatter:on
    }

    private final List<PPath> deltaFiles(IResourceDelta delta, PathMatcher matcher) throws CoreException {
        final List<PPath> paths = new ArrayList<>();
        delta.accept(new IResourceDeltaVisitor() {
            @Override public boolean visit(IResourceDelta innerDelta) throws CoreException {
                switch(innerDelta.getKind()) {
                    case IResourceDelta.REMOVED:
                    case IResourceDelta.ADDED_PHANTOM:
                    case IResourceDelta.REMOVED_PHANTOM:
                        return false;
                }
                final PPath path = pathSrv.resolve(innerDelta.getResource());
                if(matcher.matches(path.getJavaPath())) {
                    paths.add(path);
                }
                return true;
            }
        });
        return paths;
    }
}
