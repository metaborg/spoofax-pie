package mb.spoofax.eclipse.pie;

import mb.common.message.KeyedMessages;
import mb.common.style.Styling;
import mb.common.util.CollectionView;
import mb.common.util.EnumSetView;
import mb.common.util.SetView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.NullCancelled;
import mb.pie.runtime.exec.Stats;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.transform.TransformDef;
import mb.spoofax.core.language.transform.TransformInput;
import mb.spoofax.core.language.transform.TransformSubjectType;
import mb.spoofax.core.language.transform.TransformSubjects;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.resource.EclipseResource;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.eclipse.resource.EclipseResourceRegistry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class PieRunner {
    private final Logger logger;
    private final Pie pie;
    private final EclipseResourceRegistry eclipseResourceRegistry;
    private final WorkspaceUpdate.Factory workspaceUpdateFactory;

    private @Nullable WorkspaceUpdate bottomUpWorkspaceUpdate = null;


    @Inject
    public PieRunner(
        LoggerFactory loggerFactory,
        Pie pie,
        EclipseResourceRegistry eclipseResourceRegistry,
        WorkspaceUpdate.Factory workspaceUpdateFactory
    ) {
        this.logger = loggerFactory.create(getClass());
        this.pie = pie;
        this.eclipseResourceRegistry = eclipseResourceRegistry;
        this.workspaceUpdateFactory = workspaceUpdateFactory;
    }


    // Adding/updating/removing editors.

    public <D extends IDocument & IDocumentExtension4> void addOrUpdateEditor(
        EclipseLanguageComponent languageComponent,
        IFile file,
        D document,
        SpoofaxEditor editor,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        logger.trace("Adding or updating editor for file '{}'", file);

        final EclipseResourcePath resourceKey = new EclipseResourcePath(file);
        eclipseResourceRegistry.addDocumentOverride(resourceKey, document, file);
        final WorkspaceUpdate workspaceUpdate = workspaceUpdateFactory.create(languageComponent);

        try(final PieSession session = languageComponent.newPieSession()) {
            // First run a bottom-up build, to ensure that tasks affected by changed file are brought up-to-date.
            final HashSet<EclipseResourcePath> changedResources = new HashSet<>();
            changedResources.add(resourceKey);
            updateAffectedBy(changedResources, session, monitor);

            final LanguageInstance languageInstance = languageComponent.getLanguageInstance();

            final Task<@Nullable Styling> styleTask = languageInstance.createStyleTask(resourceKey);
            final String text = document.get();
            final @Nullable Styling styling = requireWithoutObserving(styleTask, session, monitor);
            //noinspection ConstantConditions (styling can really be null)
            if(styling != null) {
                workspaceUpdate.updateStyle(editor, text, styling);
            } else {
                workspaceUpdate.removeStyle(editor, text.length());
            }

            final Task<KeyedMessages> checkTask = languageInstance.createCheckTask(resourceKey);
            final KeyedMessages messages = requireWithoutObserving(checkTask, session, monitor);
            workspaceUpdate.clearMessages(resourceKey);
            workspaceUpdate.replaceMessages(messages);
        }

        workspaceUpdate.update(file, monitor);
    }

    public void removeEditor(IFile file) {
        logger.trace("Removing editor for file '{}'", file);
        final EclipseResourcePath resourceKey = new EclipseResourcePath(file);
        eclipseResourceRegistry.removeDocumentOverride(resourceKey);
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
            observeAutoTransforms(languageComponent, resourceChanges, session, monitor);
        }
    }

    public void incrementalBuild(
        EclipseLanguageComponent languageComponent,
        IProject project,
        IResourceDelta delta,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException, CoreException {
        logger.trace("Running incremental build for project '{}'", project);

        final ResourceChanges resourceChanges = new ResourceChanges(delta, languageComponent.getLanguageInstance().getFileExtensions());

        bottomUpWorkspaceUpdate = workspaceUpdateFactory.create(languageComponent);
        try(final PieSession session = languageComponent.newPieSession()) {
            updateAffectedBy(resourceChanges.changed, session, monitor);
            observeAutoTransforms(languageComponent, resourceChanges, session, monitor);
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
                unobserve(def.createTask(new TransformInput(TransformSubjects.project(project.getKey()))), pie, session);
            }
            for(ResourcePath directory : resourceChanges.newDirectories) {
                for(TransformDef def : autoTransformDefs.directory) {
                    unobserve(def.createTask(new TransformInput(TransformSubjects.directory(directory))), pie, session);
                }
            }
            for(ResourcePath file : resourceChanges.newFiles) {
                for(TransformDef def : autoTransformDefs.file) {
                    unobserve(def.createTask(new TransformInput(TransformSubjects.file(file))), pie, session);
                }
            }
            // Delete unobserved tasks (garbage collection).
            session.deleteUnobservedTasks((t) -> true, (t, r) -> true);
        }
    }


    // Startup

    public void startup(
        EclipseLanguageComponent languageComponent,
        @Nullable IProgressMonitor monitor
    ) throws IOException, CoreException, ExecException, InterruptedException {
        final ResourceChanges resourceChanges = new ResourceChanges(languageComponent.getEclipseIdentifiers().getNature(), languageComponent.getLanguageInstance().getFileExtensions());
        try(final PieSession session = languageComponent.newPieSession()) {
            observeAutoTransforms(languageComponent, resourceChanges, session, monitor);
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
                unobserve(checkTask, pie, session);
            }
        }
        workspaceUpdate.update(null, monitor);
    }


    // Standard PIE operations with trace logging.

    public <T extends Serializable> T requireWithoutObserving(Task<T> task, PieSession session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Require (without observing) '{}'", task);
        Stats.reset();
        final T result = session.requireWithoutObserving(task, monitorCancelled(monitor));
        logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
        return result;
    }

    public <T extends Serializable> T require(Task<T> task, PieSession session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Require '{}'", task);
        Stats.reset();
        final T result = session.require(task, monitorCancelled(monitor));
        logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
        return result;
    }

    public void updateAffectedBy(Set<? extends ResourceKey> changedResources, PieSession session, @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException {
        logger.trace("Update affected by '{}'", changedResources);
        Stats.reset();
        session.updateAffectedBy(changedResources, monitorCancelled(monitor));
        logger.trace("Executed/required {}/{} tasks", Stats.executions, Stats.callReqs);
    }

    public void unobserve(Task<?> task, Pie pie, PieSession session) {
        final TaskKey key = task.key();
        if(!pie.isObserved(key)) return;
        logger.trace("Unobserving '{}'", key);
        session.unobserve(key);
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
        public final HashSet<ResourcePath> changed = new HashSet<>();
        public final ArrayList<ResourcePath> newProjects = new ArrayList<>();
        public final ArrayList<ResourcePath> newDirectories = new ArrayList<>();
        public final ArrayList<ResourcePath> newFiles = new ArrayList<>();

        public ResourceChanges(EclipseResource project, SetView<String> extensions) throws IOException {
            walkProject(project, extensions);
        }

        public ResourceChanges(IResourceDelta delta, SetView<String> extensions) throws CoreException {
            delta.accept((d) -> {
                final IResource resource = d.getResource();
                final EclipseResourcePath path = new EclipseResourcePath(resource);
                changed.add(path); // Also mark non-language resources as changed, since tasks may require/provide non-language resources.
                if(d.getKind() == IResourceDelta.ADDED) {
                    switch(resource.getType()) {
                        case IResource.PROJECT:
                            newProjects.add(path);
                            break;
                        case IResource.FOLDER:
                            newDirectories.add(path);
                            break;
                        case IResource.FILE:
                            final @Nullable String extension = path.getLeafExtension();
                            if(extension != null) {
                                if(extensions.contains(extension)) {
                                    newFiles.add(path);
                                }
                            }
                            break;
                    }
                }
                return true;
            });
        }

        public ResourceChanges(String projectNatureId, SetView<String> extensions) throws IOException, CoreException {
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
        public final CollectionView<TransformDef> project;
        public final CollectionView<TransformDef> directory;
        public final CollectionView<TransformDef> file;

        public AutoTransformDefs(EclipseLanguageComponent languageComponent) {
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

    private void observeAutoTransforms(
        EclipseLanguageComponent languageComponent,
        ResourceChanges resourceChanges,
        PieSession session,
        @Nullable IProgressMonitor monitor
    ) throws ExecException, InterruptedException {
        final AutoTransformDefs autoTransformDefs = new AutoTransformDefs(languageComponent); // OPTO: calculate once per language component
        for(ResourcePath project : resourceChanges.newProjects) {
            for(TransformDef def : autoTransformDefs.project) {
                require(def.createTask(new TransformInput(TransformSubjects.project(project))), session, monitor);
            }
        }
        for(ResourcePath directory : resourceChanges.newDirectories) {
            for(TransformDef def : autoTransformDefs.directory) {
                require(def.createTask(new TransformInput(TransformSubjects.directory(directory))), session, monitor);
            }
        }
        for(ResourcePath file : resourceChanges.newFiles) {
            for(TransformDef def : autoTransformDefs.file) {
                require(def.createTask(new TransformInput(TransformSubjects.file(file))), session, monitor);
            }
        }
    }
}
