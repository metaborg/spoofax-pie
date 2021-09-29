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
import mb.pie.api.Interactivity;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.Session;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.NullCancelableToken;
import mb.pie.dagger.PieComponent;
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
import mb.spoofax.core.platform.PlatformScope;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.command.CommandUtil;
import mb.spoofax.eclipse.editor.NamedEditorInput;
import mb.spoofax.eclipse.editor.PartClosedCallback;
import mb.spoofax.eclipse.editor.SpoofaxEditorBase;
import mb.spoofax.eclipse.resource.EclipseResource;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.eclipse.resource.EclipseResourceRegistry;
import mb.spoofax.eclipse.testrunner.TestRunViewPart;
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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

@PlatformScope
public class PieRunner {
    private final Logger logger;
    private final ArgConverters argConverters;
    private final EclipseResourceRegistry resourceRegistry;
    private final WorkspaceUpdate.Factory workspaceUpdateFactory;
    private final ResourceUtil resourceUtil;
    private final PartClosedCallback partClosedCallback;
    private final Set<Interactivity> editorUpdateTags;

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
        this.editorUpdateTags = Interactivity.Interactive.asSingletonSet();
    }


    // Adding/updating/removing editors.

    public void addOrUpdateEditor(
        EclipseLanguageComponent languageComponent,
        Pie pie,
        @Nullable IProject eclipseProject,
        IFile eclipseFile,
        IDocument document,
        SpoofaxEditorBase editor,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        logger.trace("Adding or updating editor for '{}'", eclipseFile);

        final @Nullable ResourcePath rootDirectoryHint = eclipseProject != null ? new EclipseResourcePath(eclipseProject) : null;
        final EclipseResourcePath file = new EclipseResourcePath(eclipseFile);
        resourceRegistry.putDocumentOverride(file, document);

        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final Task<KeyedMessages> checkOneTask = createCheckOneTask(languageInstance, file, rootDirectoryHint);
        // Remove check callback before running PIE build, as this method updates the messages already.
        pie.removeCallback(checkOneTask);

        final WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent.getEclipseIdentifiers());
        try(final MixedSession session = pie.newSession()) {
            // First run a bottom-up build, to ensure that tasks affected by changed file are brought up-to-date.
            final HashSet<ResourceKey> changedResources = new HashSet<>();
            changedResources.add(file);
            final TopDownSession topDownSession = updateAffectedBy(changedResources, editorUpdateTags, session, monitor);

            final Task<Option<Styling>> styleTask = createStyleTask(languageInstance, file, rootDirectoryHint);
            final String text = document.get();
            final Option<Styling> stylingOption = getOrRequire(styleTask, topDownSession, monitor);
            stylingOption.ifElse(
                styling -> workspaceUpdate.updateStyle(editor, text, styling),
                () -> workspaceUpdate.removeStyle(editor, text.length())
            );

            final KeyedMessages messages = getOrRequire(checkOneTask, topDownSession, monitor);
            workspaceUpdate.replaceMessages(messages, file);
        }
        workspaceUpdate.update(eclipseFile, monitor);

        // Add callback (back again) that updates messages for when the check task is re-executed for other reasons.
        pie.setCallback(checkOneTask, messages -> {
            final WorkspaceUpdate callbackWorkspaceUpdate = workspaceUpdateFactory.create(languageComponent.getEclipseIdentifiers());
            callbackWorkspaceUpdate.replaceMessages(messages, file);
            callbackWorkspaceUpdate.update(eclipseFile, monitor);
        });
    }

    public void removeEditor(
        EclipseLanguageComponent languageComponent,
        PieComponent pieComponent,
        @Nullable IProject project,
        IFile file
    ) {
        removeEditor(languageComponent.getLanguageInstance(), pieComponent.getPie(), project, file);
    }

    public void removeEditor(
        LanguageInstance languageInstance,
        Pie pie,
        @Nullable IProject eclipseProject,
        IFile eclipseFile
    ) {
        logger.trace("Removing editor for '{}'", eclipseFile);
        final EclipseResourcePath file = new EclipseResourcePath(eclipseFile);
        final @Nullable ResourcePath rootDirectoryHint = eclipseProject != null ? new EclipseResourcePath(eclipseProject) : null;
        final Task<KeyedMessages> checkOneTask = createCheckOneTask(languageInstance, file, rootDirectoryHint);
        try(final MixedSession session = pie.newSession()) {
            unobserve(createStyleTask(languageInstance, file, rootDirectoryHint), session, null);
            unobserve(checkOneTask, session, null);
        }
        pie.removeCallback(checkOneTask);
        resourceRegistry.removeDocumentOverride(file);
    }

    private static Task<Option<Styling>> createStyleTask(LanguageInstance instance, ResourceKey file, @Nullable ResourcePath rootDirectoryHint) {
        return instance.createStyleTask(file, rootDirectoryHint);
    }

    private static Task<KeyedMessages> createCheckOneTask(LanguageInstance instance, ResourceKey file, @Nullable ResourcePath rootDirectoryHint) {
        return instance.createCheckOneTask(file, rootDirectoryHint);
    }


    // Full/incremental builds.

    public void fullBuild(
        EclipseLanguageComponent languageComponent,
        Pie pie,
        IProject eclipseProject,
        @Nullable IProgressMonitor monitor
    ) throws IOException, ExecException, InterruptedException {
        logger.trace("Running full build for project '{}'", eclipseProject);

        final EclipseResource project = new EclipseResource(resourceRegistry, eclipseProject);
        final ResourceChanges resourceChanges = new ResourceChanges(project, languageComponent.getLanguageInstance().getFileExtensions());
        resourceChanges.newProjects.add(project.getKey());

        try(final MixedSession session = pie.newSession()) {
            final TopDownSession afterSession = updateAffectedBy(resourceChanges.changed, session, monitor);
            observeAndUnobserveAutoTransforms(languageComponent, resourceChanges, pie, afterSession, monitor);
            observeAndUnobserveInspections(languageComponent, resourceChanges, pie, afterSession, monitor);
        }
    }

    public void incrementalBuild(
        EclipseLanguageComponent languageComponent,
        Pie pie,
        IProject project,
        IResourceDelta delta,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException, CoreException, IOException {
        logger.trace("Running incremental build for project '{}'", project);

        final ResourceChanges resourceChanges = new ResourceChanges(delta);
        bottomUpWorkspaceUpdate = workspaceUpdateFactory.create(languageComponent.getEclipseIdentifiers());
        try(final MixedSession session = pie.newSession()) {
            final TopDownSession afterSession = updateAffectedBy(resourceChanges.changed, session, monitor);
            observeAndUnobserveAutoTransforms(languageComponent, resourceChanges, pie, afterSession, monitor);
            observeAndUnobserveInspections(languageComponent, resourceChanges, pie, afterSession, monitor);
        }
        bottomUpWorkspaceUpdate.update(null, monitor);
        bottomUpWorkspaceUpdate = null;
    }


    // Cleans

    public void clean(
        EclipseLanguageComponent languageComponent,
        Pie pie,
        IProject eclipseProject,
        @Nullable IProgressMonitor monitor
    ) throws IOException {
        final EclipseResource projectResource = new EclipseResource(resourceRegistry, eclipseProject);
        final ResourcePath project = projectResource.getPath();
        final ResourceChanges resourceChanges = new ResourceChanges(projectResource, languageComponent.getLanguageInstance().getFileExtensions());
        resourceChanges.newProjects.add(project);
        final AutoCommandRequests autoCommandRequests = new AutoCommandRequests(languageComponent); // OPTO: calculate once per language component
        try(final MixedSession session = pie.newSession()) {
            // Unobserve auto transforms.
            for(AutoCommandRequest<?> request : autoCommandRequests.project) {
                final Task<CommandFeedback> task = request.createTask(CommandContext.ofProject(project), argConverters);
                unobserve(task, session, monitor);
            }
            for(ResourcePath directory : resourceChanges.newDirectories) {
                for(AutoCommandRequest<?> request : autoCommandRequests.directory) {
                    final Task<CommandFeedback> task = request.createTask(CommandContext.ofDirectory(directory), argConverters);
                    unobserve(task, session, monitor);
                }
            }
            for(ResourcePath file : resourceChanges.newFiles) {
                for(AutoCommandRequest<?> request : autoCommandRequests.file) {
                    final Task<CommandFeedback> task = request.createTask(CommandContext.ofFile(file), argConverters);
                    unobserve(task, session, monitor);
                }
            }
            // Unobserve inspection tasks and clear messages.
            final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
            final WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent.getEclipseIdentifiers());
            final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(projectResource.getPath());
            unobserve(checkTask, session, monitor);
            workspaceUpdate.clearMessages(project, true);
            workspaceUpdate.update(null, monitor);
            // Delete unobserved tasks and their provided files.
            deleteUnobservedTasks(session, monitor);
        }
    }


    // Startup

    public void startup(
        EclipseLanguageComponent languageComponent,
        PieComponent pieComponent,
        @Nullable IProgressMonitor monitor
    ) throws IOException, CoreException, ExecException, InterruptedException {
        for(IProject eclipseProject : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            if(!eclipseProject.isOpen() || !eclipseProject.hasNature(languageComponent.getEclipseIdentifiers().getNature()))
                continue;
            final ResourceChanges resourceChanges = new ResourceChanges(eclipseProject, languageComponent.getLanguageInstance().getFileExtensions(), resourceRegistry);
            final Pie pie = pieComponent.getPie();
            try(final MixedSession session = pie.newSession()) {
                observeAndUnobserveAutoTransforms(languageComponent, resourceChanges, pie, session, monitor);
                observeAndUnobserveInspections(languageComponent, resourceChanges, pie, session, monitor);
            }
        }
    }


    // Requiring commands

    public ArrayList<CommandContextAndFeedback> requireCommand(
        CommandRequest<?> request,
        ListView<? extends CommandContext> contexts,
        Pie pie,
        Session session,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
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
                        // TODO: this should be moved into a job to prevent deadlocks
                        try(final MixedSession newSession = pie.newSession()) {
                            unobserve(task, newSession, monitor);
                        }
                        pie.removeCallback(task);
                    });
                    if(feedback.hasErrorMessagesOrException()) {
                        // Command feedback indicates failure, unobserve to cancel continuous execution.
                        try(final MixedSession newSession = pie.newSession()) {
                            unobserve(task, newSession, monitor);
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
            .showTestResults((tests, region) -> {
                // Execute in UI thread because getActiveWorkbenchWindow is only available in the UI thread.
                Display.getDefault().asyncExec(() -> {
                    final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    final TestRunViewPart viewPart;
                    try {
                        viewPart = (TestRunViewPart)page.showView(TestRunViewPart.VIEW_ID);
                    } catch(PartInitException e) {
                        logger.error("Cannot load Test run viewpart", e);
                        return;
                    }
                    viewPart.reset();
                    viewPart.setData(tests);
                });
                return Optional.empty(); // Return value is required.
            });
    }


    // Standard PIE operations with trace logging.

    public <T extends Serializable> T requireWithoutObserving(Task<T> task, Session session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Require (without observing) '{}'", task);
        return session.requireWithoutObserving(task, monitorCancelled(monitor));
    }

    public <T extends Serializable> T require(Task<T> task, Session session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Require '{}'", task);
        return session.require(task, monitorCancelled(monitor));
    }

    public <T extends Serializable> T getOrRequire(Task<T> task, TopDownSession session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        if(session.hasBeenExecuted(task) && session.isObserved(task)) {
            logger.trace("Get '{}'", task);
            return session.getOutput(task);
        } else {
            return require(task, session, monitor);
        }
    }

    public TopDownSession updateAffectedBy(Set<? extends ResourceKey> changedResources, Set<?> tags, MixedSession session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Update affected by {} resources with tags '{}'", changedResources.size(), tags);
        return session.updateAffectedBy(changedResources, tags, monitorCancelled(monitor));
    }

    public TopDownSession updateAffectedBy(Set<? extends ResourceKey> changedResources, MixedSession session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Update affected by {} resources", changedResources.size());
        return session.updateAffectedBy(changedResources, monitorCancelled(monitor));
    }

    public void unobserve(Task<?> task, Session session, @Nullable IProgressMonitor _monitor) {
        if(!session.isObserved(task)) return;
        logger.trace("Unobserving '{}'", task);
        session.unobserve(task);
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
        WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent.getEclipseIdentifiers());
        workspaceUpdate.clearMessages(new EclipseResourcePath(project), true);
        workspaceUpdate.update(null, monitor);
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
                final @Nullable String extension = path.getLeafFileExtension();
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
        Pie pie,
        Session session,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException, IOException {
        final AutoCommandRequests autoCommandRequests = new AutoCommandRequests(languageComponent); // OPTO: calculate once per language component
        for(AutoCommandRequest<?> autoCommandRequest : autoCommandRequests.project) {
            final CommandRequest<?> request = autoCommandRequest.toCommandRequest();
            for(ResourcePath newProject : resourceChanges.newProjects) {
                requireCommand(request, CommandUtil.context(CommandContext.ofProject(newProject)), pie, session, monitor);
            }
            for(ResourcePath removedProject : resourceChanges.removedProjects) {
                final Task<CommandFeedback> task = request.createTask(CommandContext.ofProject(removedProject), argConverters);
                unobserve(task, session, monitor);
            }
        }
        for(AutoCommandRequest<?> autoCommandRequest : autoCommandRequests.directory) {
            final CommandRequest<?> request = autoCommandRequest.toCommandRequest();
            for(ResourcePath newDirectory : resourceChanges.newDirectories) {
                requireCommand(request, CommandUtil.context(CommandContext.ofDirectory(newDirectory)), pie, session, monitor);
            }
            for(ResourcePath removedDirectory : resourceChanges.removedDirectories) {
                final Task<CommandFeedback> task = request.createTask(CommandContext.ofDirectory(removedDirectory), argConverters);
                unobserve(task, session, monitor);
            }
        }
        for(AutoCommandRequest<?> autoCommandRequest : autoCommandRequests.file) {
            final CommandRequest<?> request = autoCommandRequest.toCommandRequest();
            for(ResourcePath newFile : resourceChanges.newFiles) {
                requireCommand(request, CommandUtil.context(CommandContext.ofFile(newFile)), pie, session, monitor);
            }
            for(ResourcePath removedFile : resourceChanges.removedFiles) {
                final Task<CommandFeedback> task = request.createTask(CommandContext.ofFile(removedFile), argConverters);
                unobserve(task, session, monitor);
            }
        }
        if(resourceChanges.hasRemovedResources()) {
            deleteUnobservedTasks(session, monitor);
        }
    }

    private void observeAndUnobserveInspections(
        EclipseLanguageComponent languageComponent,
        ResourceChanges resourceChanges,
        Pie pie,
        Session session,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
        final WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent.getEclipseIdentifiers());
        try {
            resourceChanges.newProjects.forEach(newProject -> {
                final Task<KeyedMessages> task = languageInstance.createCheckTask(newProject);
                pie.setCallback(task, messages -> {
                    if(bottomUpWorkspaceUpdate != null) {
                        bottomUpWorkspaceUpdate.replaceMessages(messages, newProject);
                    } else {
                        // Perform local messages update
                        WorkspaceUpdate localUpdate = workspaceUpdateFactory.create(languageComponent.getEclipseIdentifiers());
                        localUpdate.replaceMessages(messages, newProject);
                        localUpdate.update(null, monitor);
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
                unobserve(task, session, monitor);
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
        workspaceUpdate.update(null, monitor);
    }
}
