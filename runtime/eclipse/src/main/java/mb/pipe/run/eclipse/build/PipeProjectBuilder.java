package mb.pipe.run.eclipse.build;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

import build.pluto.builder.BuildManager;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.database.XodusDatabase;
import build.pluto.util.LogReporting;
import mb.pipe.run.core.log.ILogger;
import mb.pipe.run.core.model.Context;
import mb.pipe.run.core.model.IContext;
import mb.pipe.run.core.model.message.IMsg;
import mb.pipe.run.core.model.style.IStyling;
import mb.pipe.run.core.vfs.IResource;
import mb.pipe.run.core.vfs.VFSResource;
import mb.pipe.run.eclipse.PipePlugin;
import mb.pipe.run.eclipse.editor.Editors;
import mb.pipe.run.eclipse.editor.PipeEditor;
import mb.pipe.run.eclipse.util.StatusUtils;
import mb.pipe.run.eclipse.vfs.IEclipseResourceSrv;
import mb.pipe.run.pluto.generated.processFile;
import mb.pipe.run.pluto.generated.processString;

public class PipeProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = PipePlugin.id + ".builder";

    private final ILogger logger;
    private final IEclipseResourceSrv resourceSrv;
    private final Editors editors;
    private final Updater updater;


    public PipeProjectBuilder() {
        final Injector injector = PipePlugin.pipeFacade().injector;
        this.logger = injector.getInstance(ILogger.class).forContext(getClass());
        this.resourceSrv = injector.getInstance(IEclipseResourceSrv.class);
        this.editors = injector.getInstance(Editors.class);
        this.updater = injector.getInstance(Updater.class);
    }


    @Override protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
        throws CoreException {
        // TODO: check for relevant changes.

        final IProject project = getProject();
        logger.info("Building project " + project);
        final IResource projectDir = resourceSrv.resolve(project);
        final IContext context = new Context(projectDir);

        FileObject[] files;
        try {
            files = projectDir.fileObject().findFiles(new FileSelector() {
                @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
                    return true;
                }

                @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
                    final FileObject file = fileInfo.getFile();
                    final FileName name = file.getName();
                    final String ext = name.getExtension();
                    return ext.equals("min");
                }
            });
        } catch(FileSystemException e) {
            throw new CoreException(StatusUtils.error("Could not list files of project " + project, e));
        }
        final Collection<IResource> resources = Lists.newArrayList();
        for(FileObject file : files) {
            resources.add(new VFSResource(file));
        }

        try {
            try(final BuildManager buildManager =
                new BuildManager(new LogReporting(), XodusDatabase.createFileDatabase("pipeline-experiment"))) {
                try {
                    for(IResource file : resources) {
                        final BuildRequest<?, processFile.Output, ?, ?> req =
                            processFile.request(new processFile.Input(context, null, file, context));
                        final processFile.Output output = buildManager.requireInitially(req).getBuildResult();

                        logger.info("Updating file {}", file);
                        final Collection<IMsg> messages = (Collection<IMsg>) output.getPipeVal().get(4);
                        updater.updateMessagesSync(project, messages, monitor);
                    }
                    for(PipeEditor editor : editors.editors()) {
                        final String text = editor.text();
                        final BuildRequest<?, processString.Output, ?, ?> req =
                            processString.request(new processString.Input(context, null, text, context));
                        final processString.Output output = buildManager.requireInitially(req).getBuildResult();

                        logger.info("Updating editor {}", editor.name());
                        final Collection<IMsg> messages = (Collection<IMsg>) output.getPipeVal().get(3);
                        updater.updateMessagesSync(editor.eclipseResource(), messages, monitor);
                        final @Nullable IStyling styling = (IStyling) output.getPipeVal().get(4);
                        updater.updateStyle(editor.sourceViewer(), styling, monitor);
                    }
                } catch(Throwable e) {
                    throw new CoreException(
                        StatusUtils.error("Could not keep files and editors of project " + project + " consistent", e));
                }
            }
        } catch(IOException e) {
            throw new CoreException(StatusUtils.error("Could not clean up Pluto build manager", e));
        }

        return null;
    }
}
