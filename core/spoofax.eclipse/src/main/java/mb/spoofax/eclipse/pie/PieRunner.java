package mb.spoofax.eclipse.pie;

import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.style.Styling;
import mb.common.util.CollectionView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.common.util.UncheckedException;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.Session;
import mb.pie.api.Task;
import mb.pie.api.TaskKey;
import mb.pie.api.TopDownSession;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.NullCancelableToken;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandRequest;
import mb.spoofax.core.language.command.HierarchicalResourceType;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.spoofax.core.language.command.arg.ArgConverters;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.command.CommandUtil;
import mb.spoofax.eclipse.editor.NamedEditorInput;
import mb.spoofax.eclipse.editor.PartClosedCallback;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.EclipseResource;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.eclipse.resource.EclipseResourceRegistry;
import mb.spoofax.eclipse.util.ResourceUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Singleton
public class PieRunner {
    private final Logger logger;
    private final ArgConverters argConverters;
    private final EclipseResourceRegistry resourceRegistry;
    private final WorkspaceUpdate.Factory workspaceUpdateFactory;
    private final ResourceUtil resourceUtil;
    private final PartClosedCallback partClosedCallback;

    private @Nullable WorkspaceUpdate bottomUpWorkspaceUpdate = null;


    @Inject
    public PieRunner(
        LoggerFactory loggerFactory,
        ArgConverters argConverters,
        EclipseResourceRegistry resourceRegistry,
        WorkspaceUpdate.Factory workspaceUpdateFactory,
        ResourceUtil resourceUtil,
        PartClosedCallback partClosedCallback
    ) {
        this.logger = loggerFactory.create(getClass());
        this.argConverters = argConverters;
        this.resourceUtil = resourceUtil;
        this.resourceRegistry = resourceRegistry;
        this.workspaceUpdateFactory = workspaceUpdateFactory;
        this.partClosedCallback = partClosedCallback;
    }


    // Adding/updating/removing editors.

    public <D extends IDocument & IDocumentExtension4> void addOrUpdateEditor(
        EclipseLanguageComponent languageComponent,
        @Nullable IProject project,
        IFile file,
        IDocument document,
        SpoofaxEditor editor,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        logger.trace("Adding or updating editor for '{}'", file);

        final EclipseResourcePath path = new EclipseResourcePath(file);
        resourceRegistry.putDocumentOverride(path, document);

        final WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent);

        try(final MixedSession session = languageComponent.getPie().newSession()) {
            // First run a bottom-up build, to ensure that tasks affected by changed file are brought up-to-date.
            final HashSet<ResourceKey> changedResources = new HashSet<>();
            changedResources.add(path);
            final TopDownSession postSession = updateAffectedBy(changedResources, session, monitor);

            final LanguageInstance languageInstance = languageComponent.getLanguageInstance();

            final Task<Option<Styling>> styleTask = languageInstance.createStyleTask(path);
            final String text = document.get();
            final Option<Styling> stylingOption = requireWithoutObserving(styleTask, postSession, monitor);
            stylingOption.ifElse(styling -> {
                workspaceUpdate.updateStyle(editor, text, styling);
            }, () -> {
                workspaceUpdate.removeStyle(editor, text.length());
            });

            try {
                if(project == null) {
                    logger.warn("Cannot run inspections for resource '\" + file + \"' of language '\" + languageInstance.getDisplayName() + \"', because it requires multi-file analysis but no project was given");
                } else {
                    requireCheck(project, monitor, workspaceUpdate, postSession, languageInstance);
                }
            } catch(UncheckedException e) {
                final Exception cause = e.getCause();
                if(cause instanceof ExecException) {
                    throw (ExecException)cause;
                }
                if(cause instanceof InterruptedException) {
                    throw (InterruptedException)cause;
                }
                throw e;
            }
        }

        workspaceUpdate.update(file, file, monitor);
    }

    public WorkspaceUpdate requireCheck(IProject project, @Nullable IProgressMonitor monitor, TopDownSession session, EclipseLanguageComponent languageComponent) throws ExecException, InterruptedException {
        WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent);
        LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        requireCheck(project, monitor, workspaceUpdate, session, languageInstance);
        return workspaceUpdate;
    }

    private void requireCheck(IProject project, @Nullable IProgressMonitor monitor, WorkspaceUpdate workspaceUpdate, TopDownSession session, LanguageInstance languageInstance) throws ExecException, InterruptedException {
        final EclipseResourcePath resourcePath = new EclipseResourcePath(project);
        final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(resourcePath);
        final KeyedMessages messages = requireWithoutObserving(checkTask, session, monitor);
        workspaceUpdate.replaceMessages(messages, resourcePath);
    }

    public void removeEditor(IFile file) {
        logger.trace("Removing editor for '{}'", file);
        resourceRegistry.removeDocumentOverride(new EclipseResourcePath(file));
    }


    // Full/incremental builds.

    public void fullBuild(
        EclipseLanguageComponent languageComponent,
        IProject eclipseProject,
        @Nullable IProgressMonitor monitor
    ) throws IOException, ExecException, InterruptedException {
        logger.trace("Running full build for project '{}'", eclipseProject);

        final EclipseResource project = new EclipseResource(resourceRegistry, eclipseProject);
        final ResourceChanges resourceChanges = new ResourceChanges(project, languageComponent.getLanguageInstance().getFileExtensions());
        resourceChanges.newProjects.add(project.getKey());

        try(final MixedSession session = languageComponent.getPie().newSession()) {
            final TopDownSession afterSession = updateAffectedBy(resourceChanges.changed, session, monitor);
            observeAndUnobserveAutoTransforms(languageComponent, resourceChanges, afterSession, monitor);
            observeAndUnobserveInspections(languageComponent, resourceChanges, afterSession, monitor);
        }
    }

    public void incrementalBuild(
        EclipseLanguageComponent languageComponent,
        IProject project,
        IResourceDelta delta,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException, CoreException, IOException {
        logger.trace("Running incremental build for project '{}'", project);

        final ResourceChanges resourceChanges = new ResourceChanges(delta);
        bottomUpWorkspaceUpdate = workspaceUpdateFactory.create(languageComponent);
        try(final MixedSession session = languageComponent.getPie().newSession()) {
            final TopDownSession afterSession = updateAffectedBy(resourceChanges.changed, session, monitor);
            observeAndUnobserveAutoTransforms(languageComponent, resourceChanges, afterSession, monitor);
            observeAndUnobserveInspections(languageComponent, resourceChanges, afterSession, monitor);
        }
        bottomUpWorkspaceUpdate.update(project, null, monitor);
        bottomUpWorkspaceUpdate = null;
    }


    // Cleans

    public void clean(
        EclipseLanguageComponent languageComponent,
        IProject eclipseProject,
        @Nullable IProgressMonitor monitor
    ) throws IOException {
        final Pie pie = languageComponent.getPie();
        final EclipseResource projectResource = new EclipseResource(resourceRegistry, eclipseProject);
        final ResourcePath project = projectResource.getPath();
        final ResourceChanges resourceChanges = new ResourceChanges(projectResource, languageComponent.getLanguageInstance().getFileExtensions());
        resourceChanges.newProjects.add(project);
        final AutoCommandRequests autoCommandRequests = new AutoCommandRequests(languageComponent); // OPTO: calculate once per language component
        try(final MixedSession session = languageComponent.getPie().newSession()) {
            // Unobserve auto transforms.
            for(AutoCommandRequest<?> request : autoCommandRequests.project) {
                final Task<CommandFeedback> task = request.createTask(CommandContext.ofProject(project), argConverters);
                unobserve(task, pie, session, monitor);
            }
            for(ResourcePath directory : resourceChanges.newDirectories) {
                for(AutoCommandRequest<?> request : autoCommandRequests.directory) {
                    final Task<CommandFeedback> task = request.createTask(CommandContext.ofDirectory(directory), argConverters);
                    unobserve(task, pie, session, monitor);
                }
            }
            for(ResourcePath file : resourceChanges.newFiles) {
                for(AutoCommandRequest<?> request : autoCommandRequests.file) {
                    final Task<CommandFeedback> task = request.createTask(CommandContext.ofFile(file), argConverters);
                    unobserve(task, pie, session, monitor);
                }
            }
            // Unobserve inspection tasks and clear messages.
            final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
            final WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent);
            final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(projectResource.getPath());
            unobserve(checkTask, pie, session, monitor);
            workspaceUpdate.clearMessages(project, true);
            workspaceUpdate.update(eclipseProject, null, monitor);
            // Delete unobserved tasks and their provided files.
            deleteUnobservedTasks(session, monitor);
        }
    }


    // Startup

    public void startup(
        EclipseLanguageComponent languageComponent,
        @Nullable IProgressMonitor monitor
    ) throws IOException, CoreException, ExecException, InterruptedException {
        for(IProject eclipseProject : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            if(!eclipseProject.hasNature(languageComponent.getEclipseIdentifiers().getNature())) continue;
            final ResourceChanges resourceChanges = new ResourceChanges(eclipseProject, languageComponent.getLanguageInstance().getFileExtensions(), resourceRegistry);
            try(final MixedSession session = languageComponent.getPie().newSession()) {
                observeAndUnobserveAutoTransforms(languageComponent, resourceChanges, session, monitor);
                observeAndUnobserveInspections(languageComponent, resourceChanges, session, monitor);
            }
        }
    }


    // Requiring commands

    public ArrayList<CommandContextAndFeedback> requireCommand(
        EclipseLanguageComponent languageComponent,
        CommandRequest<?> request,
        ListView<? extends CommandContext> contexts,
        Session session,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        final Pie pie = languageComponent.getPie();
        final ArrayList<CommandContextAndFeedback> contextsAndFeedbacks = new ArrayList<>();
        switch(request.executionType()) {
            case ManualOnce:
                for(CommandContext context : contexts) {
                    final Task<CommandFeedback> task = request.createTask(context, argConverters);
                    final CommandFeedback feedback = requireWithoutObserving(task, session, monitor);
                    processShowFeedbacks(feedback, true, null);
                    contextsAndFeedbacks.add(new CommandContextAndFeedback(context, feedback));
                }
                break;
            case ManualContinuous:
                for(CommandContext context : contexts) {
                    final Task<CommandFeedback> task = request.createTask(context, argConverters);
                    final CommandFeedback feedback = require(task, session, monitor);
                    contextsAndFeedbacks.add(new CommandContextAndFeedback(context, feedback));
                    processShowFeedbacks(feedback, true, (p) -> {
                        // POTI: this opens a new PIE session, which may be used concurrently with other sessions, which
                        // may not be (thread-)safe.
                        try(final MixedSession newSession = languageComponent.getPie().newSession()) {
                            unobserve(task, pie, newSession, monitor);
                        }
                        pie.removeCallback(task);
                    });
                    if(feedback.hasErrorMessagesOrException()) {
                        // Command feedback indicates failure, unobserve to cancel continuous execution.
                        try(final MixedSession newSession = languageComponent.getPie().newSession()) {
                            unobserve(task, pie, newSession, monitor);
                        }
                    } else {
                        // Command feedback indicates success, set a callback to process feedback when task is required.
                        pie.setCallback(task, (o) -> processShowFeedbacks(o, false, null));
                    }
                }
                break;
            case AutomaticContinuous:
                // TODO: remove AutomaticContinuous builders, they should just be hooked into a compile task.
                for(CommandContext context : contexts) {
                    final Task<CommandFeedback> task = request.createTask(context, argConverters);
                    require(task, session, monitor);
                }
                break;
        }
        return contextsAndFeedbacks;
    }

    private void processShowFeedbacks(CommandFeedback feedback, boolean activate, @Nullable Consumer<IWorkbenchPart> closedCallback) {
        for(ShowFeedback showFeedback : feedback.getShowFeedbacks()) {
            processShowFeedback(showFeedback, activate, closedCallback);
        }
    }

    private void processShowFeedback(ShowFeedback showFeedback, boolean activate, @Nullable Consumer<IWorkbenchPart> closedCallback) {
        showFeedback.caseOf()
            .showFile((file, region) -> {
                final IFile eclipseFile = resourceUtil.getEclipseFile(file);
                // Execute in UI thread because getActiveWorkbenchWindow is only available in the UI thread.
                Display.getDefault().asyncExec(() -> {
                    final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    try {
                        final IEditorPart editor = IDE.openEditor(page, eclipseFile, activate);
                        if(closedCallback != null) {
                            partClosedCallback.addCallback(editor, closedCallback);
                        }
                        //noinspection ConstantConditions (region can really be null)
                        if(region != null && editor instanceof ITextEditor) {
                            final ITextEditor textEditor = (ITextEditor)editor;
                            textEditor.selectAndReveal(region.getStartOffset(), region.getLength());
                        }
                    } catch(PartInitException e) {
                        throw new RuntimeException("Cannot open editor for file '" + file + "', opening editor failed unexpectedly", e);
                    }
                });

                return Optional.empty(); // Return value is required.
            })
            .showText((text, name, region) -> {
                final NamedEditorInput editorInput = new NamedEditorInput(name);

                // Execute in UI thread because getActiveWorkbenchWindow is only available in the UI thread.
                Display.getDefault().asyncExec(() -> {
                    try {
                        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        final IEditorPart editor = IDE.openEditor(page, editorInput, EditorsUI.DEFAULT_TEXT_EDITOR_ID, activate);
                        if(closedCallback != null) {
                            partClosedCallback.addCallback(editor, closedCallback);
                        }
                        if(editor instanceof ITextEditor) {
                            final ITextEditor textEditor = (ITextEditor)editor;
                            final @Nullable IDocumentProvider documentProvider = textEditor.getDocumentProvider();
                            if(documentProvider == null) {
                                logger.error("Cannot update text of editor with name '" + name + "', getDocumentProvider returns null");
                                return;
                            }
                            final @Nullable IDocument document = documentProvider.getDocument(editorInput);
                            if(document == null) {
                                logger.error("Cannot update text of editor with name '" + name + "', getDocument returns null");
                                return;
                            }
                            document.set(text);
                            //noinspection ConstantConditions (region can really be null)
                            if(region != null) {
                                textEditor.selectAndReveal(region.getStartOffset(), region.getLength());
                            }
                        } else {
                            logger.error("Cannot update text of editor with name '" + name + "', it does not implement ITextEditor");
                        }
                    } catch(PartInitException e) {
                        throw new RuntimeException("Cannot open editor (for text) with name '" + name + "', opening editor failed unexpectedly", e);
                    }
                });

                return Optional.empty(); // Return value is required.
            })
        ;
    }


    // Standard PIE operations with trace logging.

    public <T extends @Nullable Serializable> T requireWithoutObserving(Task<T> task, Session session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Require (without observing) '{}'", task);
        final T result = session.requireWithoutObserving(task, monitorCancelled(monitor));
        return result;
    }

    public <T extends @Nullable Serializable> T require(Task<T> task, Session session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Require '{}'", task);
        final T result = session.require(task, monitorCancelled(monitor));
        return result;
    }

    public TopDownSession updateAffectedBy(Set<? extends ResourceKey> changedResources, MixedSession session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Update affected by '{}'", changedResources);
        final TopDownSession newSession = session.updateAffectedBy(changedResources, monitorCancelled(monitor));
        return newSession;
    }

    public void unobserve(Task<?> task, Pie pie, Session session, @Nullable IProgressMonitor _monitor) {
        final TaskKey key = task.key();
        if(!pie.isObserved(key)) return;
        logger.trace("Unobserving '{}'", key);
        session.unobserve(key);
    }

    public void deleteUnobservedTasks(Session session, @Nullable IProgressMonitor _monitor) throws IOException {
        logger.trace("Deleting unobserved tasks");
        session.deleteUnobservedTasks((t) -> true, (t, r) -> true);
    }


    // Helper/utility classes and methods.

    private static CancelToken monitorCancelled(@Nullable IProgressMonitor monitor) {
        if(monitor != null) {
            return new MonitorCancelableToken(monitor);
        } else {
            return NullCancelableToken.instance;
        }
    }

    public void clearMessages(IProject project, @Nullable IProgressMonitor monitor, EclipseLanguageComponent languageComponent) {
        WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent);
        workspaceUpdate.clearMessages(new EclipseResourcePath(project), true);
        workspaceUpdate.update(project, null, monitor);
    }


    private static class ResourceChanges {
        final HashSet<ResourcePath> changed = new HashSet<>();
        final ArrayList<ResourcePath> newProjects = new ArrayList<>();
        final ArrayList<ResourcePath> newDirectories = new ArrayList<>();
        final ArrayList<ResourcePath> newFiles = new ArrayList<>();
        final ArrayList<ResourcePath> removedProjects = new ArrayList<>();
        final ArrayList<ResourcePath> removedDirectories = new ArrayList<>();
        final ArrayList<ResourcePath> removedFiles = new ArrayList<>();

        boolean hasRemovedResources() {
            return !(removedProjects.isEmpty() && removedDirectories.isEmpty() && removedFiles.isEmpty());
        }

        ResourceChanges(EclipseResource project, SetView<String> extensions) throws IOException {
            walkProject(project, extensions);
        }

        ResourceChanges(IResourceDelta delta) throws CoreException {
            walkDelta(delta, path -> true);
        }

        ResourceChanges(IResourceDelta delta, SetView<String> extensions) throws CoreException {
            walkDelta(delta, path -> {
                final @Nullable String extension = path.getLeafExtension();
                return extension != null && extensions.contains(extension);
            });
        }

        ResourceChanges(IProject eclipseProject, SetView<String> extensions, EclipseResourceRegistry resourceRegistry) throws IOException {
            final EclipseResource project = new EclipseResource(resourceRegistry, eclipseProject);
            newProjects.add(project.getKey());
            walkProject(project, extensions);
        }

        private void walkDelta(IResourceDelta delta, Predicate<ResourcePath> filePredicate) throws CoreException {
            delta.accept((d) -> {
                final int kind = d.getKind();
                final boolean added = kind == IResourceDelta.ADDED;
                final boolean removed = kind == IResourceDelta.REMOVED;
                final IResource resource = d.getResource();
                final EclipseResourcePath path = new EclipseResourcePath(resource);
                // Do not mark removed resources as changed, as tasks for removed resources should be unobserved instead
                // of being executed. POTI: what about tasks that want to be executed on removed resources?
                if(!removed) {
                    // Mark all resources as changed, since tasks may require/provide non-language resources.
                    changed.add(path);
                }
                switch(resource.getType()) {
                    case IResource.PROJECT:
                        if(added) {
                            newProjects.add(path);
                        } else if(removed) {
                            removedProjects.add(path);
                        }
                        break;
                    case IResource.FOLDER:
                        if(added) {
                            newDirectories.add(path);
                        } else if(removed) {
                            removedDirectories.add(path);
                        }
                        break;
                    case IResource.FILE:
                        if(filePredicate.test(path)) {
                            if(added) {
                                newFiles.add(path);
                            } else if(removed) {
                                removedFiles.add(path);
                            }
                        }
                        break;
                }
                return true;
            });
        }

        private void walkProject(EclipseResource project, SetView<String> extensions) throws IOException {
            changed.add(project.getKey());
            project.walk().forEach((r) -> {
                final ResourcePath key = r.getKey();
                changed.add(key);
                switch(r.getType()) {
                    case File:
                        final @Nullable String extension = r.getLeafExtension();
                        if(extension != null && extensions.contains(extension)) {
                            newFiles.add(key);
                        }
                        break;
                    case Directory:
                        newDirectories.add(key);
                        break;
                    case Unknown:
                        break;
                }
            });
        }
    }


    private static class AutoCommandRequests {
        final CollectionView<AutoCommandRequest<?>> project;
        final CollectionView<AutoCommandRequest<?>> directory;
        final CollectionView<AutoCommandRequest<?>> file;

        AutoCommandRequests(EclipseLanguageComponent languageComponent) {
            final ArrayList<AutoCommandRequest<?>> project = new ArrayList<>();
            final ArrayList<AutoCommandRequest<?>> directory = new ArrayList<>();
            final ArrayList<AutoCommandRequest<?>> file = new ArrayList<>();
            for(AutoCommandRequest<?> request : languageComponent.getLanguageInstance().getAutoCommandRequests()) {
                final Set<HierarchicalResourceType> resourceTypes = request.resourceTypes();
                if(resourceTypes.contains(HierarchicalResourceType.Project)) {
                    project.add(request);
                }
                if(resourceTypes.contains(HierarchicalResourceType.Directory)) {
                    directory.add(request);
                }
                if(resourceTypes.contains(HierarchicalResourceType.File)) {
                    file.add(request);
                }
            }
            this.project = new CollectionView<>(project);
            this.directory = new CollectionView<>(directory);
            this.file = new CollectionView<>(file);
        }
    }

    private void observeAndUnobserveAutoTransforms(
        EclipseLanguageComponent languageComponent,
        ResourceChanges resourceChanges,
        Session session,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException, IOException {
        final Pie pie = languageComponent.getPie();
        final AutoCommandRequests autoCommandRequests = new AutoCommandRequests(languageComponent); // OPTO: calculate once per language component
        for(AutoCommandRequest<?> autoCommandRequest : autoCommandRequests.project) {
            final CommandRequest<?> request = autoCommandRequest.toCommandRequest();
            for(ResourcePath newProject : resourceChanges.newProjects) {
                requireCommand(languageComponent, request, CommandUtil.context(CommandContext.ofProject(newProject)), session, monitor);
            }
            for(ResourcePath removedProject : resourceChanges.removedProjects) {
                final Task<CommandFeedback> task = request.createTask(CommandContext.ofProject(removedProject), argConverters);
                unobserve(task, pie, session, monitor);
            }
        }
        for(AutoCommandRequest<?> autoCommandRequest : autoCommandRequests.directory) {
            final CommandRequest<?> request = autoCommandRequest.toCommandRequest();
            for(ResourcePath newDirectory : resourceChanges.newDirectories) {
                requireCommand(languageComponent, request, CommandUtil.context(CommandContext.ofDirectory(newDirectory)), session, monitor);
            }
            for(ResourcePath removedDirectory : resourceChanges.removedDirectories) {
                final Task<CommandFeedback> task = request.createTask(CommandContext.ofDirectory(removedDirectory), argConverters);
                unobserve(task, pie, session, monitor);
            }
        }
        for(AutoCommandRequest<?> autoCommandRequest : autoCommandRequests.file) {
            final CommandRequest<?> request = autoCommandRequest.toCommandRequest();
            for(ResourcePath newFile : resourceChanges.newFiles) {
                requireCommand(languageComponent, request, CommandUtil.context(CommandContext.ofFile(newFile)), session, monitor);
            }
            for(ResourcePath removedFile : resourceChanges.removedFiles) {
                final Task<CommandFeedback> task = request.createTask(CommandContext.ofFile(removedFile), argConverters);
                unobserve(task, pie, session, monitor);
            }
        }
        if(resourceChanges.hasRemovedResources()) {
            deleteUnobservedTasks(session, monitor);
        }
    }

    private void observeAndUnobserveInspections(
        EclipseLanguageComponent languageComponent,
        ResourceChanges resourceChanges,
        Session session,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        final Pie pie = languageComponent.getPie();
        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent);
        try {
            resourceChanges.newProjects.forEach(newProject -> {
                final Task<KeyedMessages> task = languageInstance.createCheckTask(newProject);
                pie.setCallback(task, messages -> {
                    if(bottomUpWorkspaceUpdate != null) {
                        bottomUpWorkspaceUpdate.replaceMessages(messages, newProject);
                    } else {
                        // Perform local messages update
                        WorkspaceUpdate localUpdate = workspaceUpdateFactory.create(languageComponent);
                        localUpdate.replaceMessages(messages, newProject);
                        localUpdate.update(resourceUtil.getEclipseResource(newProject), null, monitor);
                    }
                });
                if(!pie.isObserved(task)) {
                    try {
                        final KeyedMessages messages = require(task, session, monitor);
                        workspaceUpdate.replaceMessages(messages, newProject);
                    } catch(InterruptedException | ExecException e) {
                        throw new UncheckedException(e);
                    }
                }
            });
            resourceChanges.removedProjects.forEach(removedProject -> {
                final Task<KeyedMessages> task = languageInstance.createCheckTask(removedProject);
                unobserve(task, pie, session, monitor);
                workspaceUpdate.clearMessages(removedProject, true);
            });
        } catch(UncheckedException e) {
            final Exception cause = e.getCause();
            if(cause instanceof ExecException) {
                throw (ExecException)cause;
            }
            if(cause instanceof InterruptedException) {
                throw (InterruptedException)cause;
            }
            throw e;
        }
        workspaceUpdate.update(ResourcesPlugin.getWorkspace().getRoot(), null, monitor);
    }
}
