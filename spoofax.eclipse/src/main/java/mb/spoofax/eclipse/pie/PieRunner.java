package mb.spoofax.eclipse.pie;

import mb.common.message.KeyedMessages;
import mb.common.style.Styling;
import mb.common.util.CollectionView;
import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.NullCancelled;
import mb.pie.runtime.exec.Stats;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.transform.*;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.editor.NamedEditorInput;
import mb.spoofax.eclipse.editor.PartClosedCallback;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.*;
import mb.spoofax.eclipse.transform.TransformUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
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

@Singleton
public class PieRunner {
    private final Logger logger;
    private final ResourceService resourceService;
    private final Pie pie;
    private final EclipseDocumentResourceRegistry eclipseDocumentResourceRegistry;
    private final WorkspaceUpdate.Factory workspaceUpdateFactory;
    private final PartClosedCallback partClosedCallback;

    private @Nullable WorkspaceUpdate bottomUpWorkspaceUpdate = null;


    @Inject
    public PieRunner(
        LoggerFactory loggerFactory,
        ResourceService resourceService,
        Pie pie,
        EclipseDocumentResourceRegistry eclipseDocumentResourceRegistry,

        WorkspaceUpdate.Factory workspaceUpdateFactory,
        PartClosedCallback partClosedCallback) {
        this.logger = loggerFactory.create(getClass());
        this.resourceService = resourceService;
        this.pie = pie;
        this.eclipseDocumentResourceRegistry = eclipseDocumentResourceRegistry;
        this.workspaceUpdateFactory = workspaceUpdateFactory;
        this.partClosedCallback = partClosedCallback;
    }


    // Adding/updating/removing editors.

    public <D extends IDocument & IDocumentExtension4> void addOrUpdateEditor(
        EclipseLanguageComponent languageComponent,
        EclipseDocumentResource resource,
        SpoofaxEditor editor,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        logger.trace("Adding or updating editor for '{}'", resource);

        eclipseDocumentResourceRegistry.putDocumentResource(resource);
        final EclipseDocumentKey key = resource.getKey();

        final WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent);

        try(final PieSession session = languageComponent.newPieSession()) {
            // First run a bottom-up build, to ensure that tasks affected by changed file are brought up-to-date.
            final HashSet<ResourceKey> changedResources = new HashSet<>();
            changedResources.add(key);
            updateAffectedBy(changedResources, session, monitor);

            final LanguageInstance languageInstance = languageComponent.getLanguageInstance();

            final Task<@Nullable Styling> styleTask = languageInstance.createStyleTask(key);
            final String text = resource.getDocument().get();
            final @Nullable Styling styling = requireWithoutObserving(styleTask, session, monitor);
            //noinspection ConstantConditions (styling can really be null)
            if(styling != null) {
                workspaceUpdate.updateStyle(editor, text, styling);
            } else {
                workspaceUpdate.removeStyle(editor, text.length());
            }

            final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(key);
            final KeyedMessages messages = requireWithoutObserving(checkTask, session, monitor);
            workspaceUpdate.clearMessages(key);
            workspaceUpdate.replaceMessages(messages);
        }

        workspaceUpdate.update(resource.getWrappedEclipseResource(), monitor);
    }

    public void removeEditor(EclipseDocumentResource resource) {
        logger.trace("Removing editor for '{}'", resource);
        eclipseDocumentResourceRegistry.removeDocumentResource(resource);
    }


    // Full/incremental builds.

    public void fullBuild(
        EclipseLanguageComponent languageComponent,
        IProject eclipseProject,
        @Nullable IProgressMonitor monitor
    ) throws IOException, ExecException, InterruptedException {
        logger.trace("Running full build for project '{}'", eclipseProject);

        final EclipseResource project = new EclipseResource(eclipseProject);
        final ResourceChanges resourceChanges = new ResourceChanges(project, languageComponent.getLanguageInstance().getFileExtensions());
        resourceChanges.newProjects.add(project.getKey());

        try(final PieSession session = languageComponent.newPieSession()) {
            updateAffectedBy(resourceChanges.changed, session, monitor);
            observeUnobserveAutoTransforms(languageComponent, resourceChanges, session, monitor);
        }
    }

    public void incrementalBuild(
        EclipseLanguageComponent languageComponent,
        IProject project,
        IResourceDelta delta,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException, CoreException, IOException {
        logger.trace("Running incremental build for project '{}'", project);

        final ResourceChanges resourceChanges = new ResourceChanges(delta, languageComponent.getLanguageInstance().getFileExtensions());

        bottomUpWorkspaceUpdate = workspaceUpdateFactory.create(languageComponent);
        try(final PieSession session = languageComponent.newPieSession()) {
            updateAffectedBy(resourceChanges.changed, session, monitor);
            observeUnobserveAutoTransforms(languageComponent, resourceChanges, session, monitor);
        }
        bottomUpWorkspaceUpdate.update(null, monitor);
        bottomUpWorkspaceUpdate = null;
    }


    // Cleans

    public void clean(
        EclipseLanguageComponent languageComponent,
        IProject eclipseProject,
        @Nullable IProgressMonitor monitor
    ) throws IOException {
        final EclipseResource project = new EclipseResource(eclipseProject);
        final ResourceChanges resourceChanges = new ResourceChanges(project, languageComponent.getLanguageInstance().getFileExtensions());
        resourceChanges.newProjects.add(project.getKey());
        final AutoTransformDefs autoTransformDefs = new AutoTransformDefs(languageComponent); // OPTO: calculate once per language component
        try(final PieSession session = languageComponent.newPieSession()) {
            // Unobserve auto transforms.
            for(TransformDef def : autoTransformDefs.project) {
                unobserve(def.createTask(new TransformInput(TransformSubjects.project(project.getKey()))), pie, session, monitor);
            }
            for(ResourcePath directory : resourceChanges.newDirectories) {
                for(TransformDef def : autoTransformDefs.directory) {
                    unobserve(def.createTask(new TransformInput(TransformSubjects.directory(directory))), pie, session, monitor);
                }
            }
            for(ResourcePath file : resourceChanges.newFiles) {
                for(TransformDef def : autoTransformDefs.file) {
                    unobserve(def.createTask(new TransformInput(TransformSubjects.file(file))), pie, session, monitor);
                }
            }
            deleteUnobservedTasks(session, monitor);
        }
    }


    // Startup

    public void startup(
        EclipseLanguageComponent languageComponent,
        @Nullable IProgressMonitor monitor
    ) throws IOException, CoreException, ExecException, InterruptedException {
        final ResourceChanges resourceChanges = new ResourceChanges(languageComponent.getEclipseIdentifiers().getNature(), languageComponent.getLanguageInstance().getFileExtensions());
        try(final PieSession session = languageComponent.newPieSession()) {
            observeUnobserveAutoTransforms(languageComponent, resourceChanges, session, monitor);
        }
    }


    // Observing/unobserving check tasks.

    public boolean isCheckObserved(
        EclipseLanguageComponent languageComponent,
        IFile file
    ) {
        final EclipseResourcePath resourceKey = new EclipseResourcePath(file);
        final Task<KeyedMessages> checkTask = languageComponent.getLanguageInstance().createCheckTask(resourceKey);
        return pie.isObserved(checkTask);
    }

    public void observeCheckTasks(
        EclipseLanguageComponent languageComponent,
        Iterable<IFile> files,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        final WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent);
        try(final PieSession session = languageComponent.newPieSession()) {
            final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
            for(IFile file : files) {
                final EclipseResourcePath resourceKey = new EclipseResourcePath(file);
                final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(resourceKey);
                pie.setCallback(checkTask, (messages) -> {
                    if(bottomUpWorkspaceUpdate != null) {
                        bottomUpWorkspaceUpdate.replaceMessages(messages);
                    }
                });
                if(!pie.isObserved(checkTask)) {
                    final KeyedMessages messages = require(checkTask, session, monitor);
                    workspaceUpdate.replaceMessages(messages);
                }
            }
        }
        workspaceUpdate.update(null, monitor);
    }

    public void unobserveCheckTasks(
        EclipseLanguageComponent languageComponent,
        Iterable<IFile> files,
        @Nullable IProgressMonitor monitor
    ) {
        final WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent);
        try(final PieSession session = languageComponent.newPieSession()) {
            final LanguageInstance languageInstance = languageComponent.getLanguageInstance();
            for(IFile file : files) {
                final EclipseResourcePath resourceKey = new EclipseResourcePath(file);
                final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(resourceKey);
                // BUG: this also clears messages for open editors, which it shouldn't do.
                workspaceUpdate.clearMessages(resourceKey);
                unobserve(checkTask, pie, session, monitor);
            }
        }
        workspaceUpdate.update(null, monitor);
    }


    // Requiring transforms

    public void requireTransform(
        EclipseLanguageComponent languageComponent,
        TransformDef def,
        TransformExecutionType executionType,
        ListView<TransformInput> inputs,
        PieSession session,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        switch(executionType) {
            case ManualOnce:
                for(TransformInput input : inputs) {
                    final TransformOutput output = requireWithoutObserving(def.createTask(input), session, monitor);
                    processOutput(output, true, null);
                }
                break;
            case ManualContinuous:
                for(TransformInput input : inputs) {
                    final Task<TransformOutput> task = def.createTask(input);
                    final TransformOutput output = require(task, session, monitor);
                    processOutput(output, true, (p) -> {
                        // POTI: this opens a new PIE session, which may be used concurrently with other sessions, which
                        // may not be (thread-)safe.
                        try(final PieSession newSession = languageComponent.newPieSession()) {
                            unobserve(task, pie, newSession, monitor);
                        }
                        pie.removeCallback(task);
                    });
                    pie.setCallback(task, (o) -> processOutput(o, false, null));
                }
                break;
            case AutomaticContinuous:
                for(TransformInput input : inputs) {
                    require(def.createTask(input), session, monitor);
                    // Feedback for AutomaticContinuous is ignored intentionally: do not want to suddenly open new
                    // editors when a resource is saved.
                }
                break;
        }
    }

    private void processOutput(TransformOutput output, boolean activate, @Nullable Consumer<IWorkbenchPart> closedCallback) {
        for(TransformFeedback feedback : output.feedback) {
            processFeedback(feedback, activate, closedCallback);
        }
    }

    private void processFeedback(TransformFeedback feedback, boolean activate, @Nullable Consumer<IWorkbenchPart> closedCallback) {
        TransformFeedbacks.caseOf(feedback)
            .openEditorForFile((file, region) -> {
                final @Nullable IResource eclipseResource;
                try {
                    final @Nullable WrapsEclipseResource resource = resourceService.getResource(file);
                    eclipseResource = resource.getWrappedEclipseResource();
                    if(eclipseResource == null) {
                        throw new RuntimeException("Cannot open editor for file '" + file + "', it has no corresponding Eclipse resource");
                    }
                    if(!(eclipseResource instanceof IFile)) {
                        throw new RuntimeException("Cannot open editor for Eclipse resource '" + eclipseResource + "', it is not a file");
                    }
                } catch(ClassCastException e) {
                    throw new RuntimeException("Cannot open editor for file '" + file + "', corresponding resource does not implement WrapsEclipseResource", e);
                }

                // Execute in UI thread because getActiveWorkbenchWindow is only available in the UI thread.
                Display.getDefault().asyncExec(() -> {
                    final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    try {
                        final IEditorPart editor = IDE.openEditor(page, (IFile) eclipseResource, activate);
                        if(closedCallback != null) {
                            partClosedCallback.addCallback(editor, closedCallback);
                        }
                        //noinspection ConstantConditions (region can really be null)
                        if(region != null && editor instanceof ITextEditor) {
                            final ITextEditor textEditor = (ITextEditor) editor;
                            textEditor.selectAndReveal(region.getStartOffset(), region.length());
                        }
                    } catch(PartInitException e) {
                        throw new RuntimeException("Cannot open editor for file '" + file + "', opening editor failed unexpectedly", e);
                    }
                });

                return Optional.empty(); // Return value is required.
            })
            .openEditorWithText((text, name, region) -> {
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
                            final ITextEditor textEditor = (ITextEditor) editor;
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
                                textEditor.selectAndReveal(region.getStartOffset(), region.length());
                            }
                        } else {
                            logger.error("Cannot update text of editor with name '" + name + "', it does not implement ITextEditor");
                        }
                    } catch(PartInitException e) {
                        throw new RuntimeException("Cannot open editor (for text) with name '" + name + "', opening editor failed unexpectedly", e);
                    }
                });

                return Optional.empty(); // Return value is required.
            });
    }


    // Standard PIE operations with trace logging.

    private <T extends Serializable> T requireWithoutObserving(Task<T> task, PieSession session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Require (without observing) '{}'", task);
        Stats.reset();
        final T result = session.requireWithoutObserving(task, monitorCancelled(monitor));
        logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
        return result;
    }

    private <T extends Serializable> T require(Task<T> task, PieSession session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Require '{}'", task);
        Stats.reset();
        final T result = session.require(task, monitorCancelled(monitor));
        logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
        return result;
    }

    private void updateAffectedBy(Set<? extends ResourceKey> changedResources, PieSession session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Update affected by '{}'", changedResources);
        Stats.reset();
        session.updateAffectedBy(changedResources, monitorCancelled(monitor));
        logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
    }

    private void unobserve(Task<?> task, Pie pie, PieSession session, @Nullable IProgressMonitor _monitor) {
        final TaskKey key = task.key();
        if(!pie.isObserved(key)) return;
        logger.trace("Unobserving '{}'", key);
        session.unobserve(key);
    }

    private void deleteUnobservedTasks(PieSession session, @Nullable IProgressMonitor _monitor) throws IOException {
        logger.trace("Deleting unobserved tasks");
        session.deleteUnobservedTasks((t) -> true, (t, r) -> true);
    }


    // Helper/utility classes and methods.

    private static Cancelled monitorCancelled(@Nullable IProgressMonitor monitor) {
        if(monitor != null) {
            return new MonitorCancelToken(monitor);
        } else {
            return new NullCancelled();
        }
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

        ResourceChanges(IResourceDelta delta, SetView<String> extensions) throws CoreException {
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
                        final @Nullable String extension = path.getLeafExtension();
                        if(extension != null && extensions.contains(extension)) {
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

        ResourceChanges(String projectNatureId, SetView<String> extensions) throws IOException, CoreException {
            for(IProject eclipseProject : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
                if(!eclipseProject.hasNature(projectNatureId)) continue;
                final EclipseResource project = new EclipseResource(eclipseProject);
                newProjects.add(project.getKey());
                walkProject(project, extensions);
            }
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


    private static class AutoTransformDefs {
        final CollectionView<TransformDef> project;
        final CollectionView<TransformDef> directory;
        final CollectionView<TransformDef> file;

        AutoTransformDefs(EclipseLanguageComponent languageComponent) {
            final ArrayList<TransformDef> project = new ArrayList<>();
            final ArrayList<TransformDef> directory = new ArrayList<>();
            final ArrayList<TransformDef> file = new ArrayList<>();
            for(TransformDef def : languageComponent.getLanguageInstance().getAutoTransformDefs()) {
                final EnumSetView<TransformSubjectType> supported = def.getSupportedSubjectTypes();
                if(supported.contains(TransformSubjectType.Project)) {
                    project.add(def);
                }
                if(supported.contains(TransformSubjectType.Directory)) {
                    directory.add(def);
                }
                if(supported.contains(TransformSubjectType.File)) {
                    file.add(def);
                }
            }
            this.project = new CollectionView<>(project);
            this.directory = new CollectionView<>(directory);
            this.file = new CollectionView<>(file);
        }
    }

    private void observeUnobserveAutoTransforms(
        EclipseLanguageComponent languageComponent,
        ResourceChanges resourceChanges,
        PieSession session,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException, IOException {
        final AutoTransformDefs autoTransformDefs = new AutoTransformDefs(languageComponent); // OPTO: calculate once per language component
        final TransformExecutionType executionType = TransformExecutionType.AutomaticContinuous;
        for(TransformDef def : autoTransformDefs.project) {
            for(ResourcePath newProject : resourceChanges.newProjects) {
                requireTransform(languageComponent, def, executionType, TransformUtil.input(TransformSubjects.project(newProject)), session, monitor);
            }
            for(ResourcePath removedProject : resourceChanges.removedProjects) {
                unobserve(def.createTask(new TransformInput(TransformSubjects.project(removedProject))), pie, session, monitor);
            }
        }
        for(TransformDef def : autoTransformDefs.directory) {
            for(ResourcePath newDirectory : resourceChanges.newDirectories) {
                requireTransform(languageComponent, def, executionType, TransformUtil.input(TransformSubjects.directory(newDirectory)), session, monitor);
            }
            for(ResourcePath removedDirectory : resourceChanges.removedDirectories) {
                unobserve(def.createTask(new TransformInput(TransformSubjects.directory(removedDirectory))), pie, session, monitor);
            }
        }
        for(TransformDef def : autoTransformDefs.file) {
            for(ResourcePath newFile : resourceChanges.newFiles) {
                requireTransform(languageComponent, def, executionType, TransformUtil.input(TransformSubjects.file(newFile)), session, monitor);
            }
            for(ResourcePath removedFile : resourceChanges.removedFiles) {
                unobserve(def.createTask(new TransformInput(TransformSubjects.file(removedFile))), pie, session, monitor);
            }
        }
        if(resourceChanges.hasRemovedResources()) {
            deleteUnobservedTasks(session, monitor);
        }
    }
}
