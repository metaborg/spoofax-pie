package mb.spoofax.lwb.eclipse.generator;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.eclipse.util.StatusUtil;
import mb.spoofax.lwb.compiler.generator.LanguageProjectGenerator;
import mb.spoofax.lwb.eclipse.SpoofaxLwbLifecycleParticipant;
import mb.spoofax.lwb.eclipse.SpoofaxLwbNature;
import mb.spoofax.lwb.eclipse.util.ClassPathUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class LanguageProjectWizard extends Wizard implements INewWizard {
    private final Logger logger;
    private final LanguageProjectWizardPage page;
    private final LanguageProjectGenerator languageProjectGenerator;

    public LanguageProjectWizard() {
        final LoggerFactory loggerFactory = SpoofaxPlugin.getLoggerComponent().getLoggerFactory();
        this.logger = loggerFactory.create(getClass());
        this.page = new LanguageProjectWizardPage(loggerFactory);
        this.languageProjectGenerator = SpoofaxLwbLifecycleParticipant.getInstance().getSpoofax3Compiler().component.getLanguageProjectGenerator();
        addPage(this.page);
        setNeedsProgressMonitor(true);
    }

    @Override public void init(IWorkbench workbench, IStructuredSelection selection) {}

    @Override public boolean performFinish() {
        final @Nullable IPath basePath;
        if(page.useDefaults()) {
            basePath = null;
        } else {
            basePath = page.getLocationPath();
        }

        // Access page properties here, before the IRunnableWithProgress because it runs in a different thread which may
        // not access the page properties (they may only be accessed on the UI thread)
        final String id = page.id();
        final LanguageProjectGenerator.Input.Builder inputBuilder = LanguageProjectGenerator.Input.builder()
            .id(page.id())
            .name(page.name())
            .javaClassIdPrefix(page.javaClassIdPrefix())
            .fileExtensions(page.fileExtensions())
            .multiFileAnalysis(page.multiFileAnalysis());

        // Run in IRunnableWithProgress for progress reporting in wizard dialog.
        final IRunnableWithProgress runnable = monitor -> {
            try {
                // Create project.
                final IProject project = getProject(id);
                if(project.exists()) {
                    throw new IOException("Cannot create language project because project with name '" + project + "' already exists");
                }
                createProject(project, basePath);
                // Generate with ICoreRunnable to avoid resource updates during project generation.
                final ICoreRunnable coreRunnable = new ICoreRunnable() {
                    @Override public void run(IProgressMonitor monitor) throws CoreException {
                        try {
                            generate(project, inputBuilder, monitor);
                        } catch(IOException e) {
                            throw new CoreException(StatusUtil.error("Generating language project failed", e));
                        }
                    }
                };
                // Use workspace root as scheduling rule, because adding a nature requires it.
                ResourcesPlugin.getWorkspace().run(coreRunnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, monitor);
            } catch(Throwable e) {
                throw new InvocationTargetException(e);
            } finally {
                monitor.done();
            }
        };

        try {
            getContainer().run(true, true, runnable);
        } catch(InterruptedException e) {
            return false;
        } catch(InvocationTargetException e) {
            final Throwable t = e.getTargetException();
            logger.error("Generating project failed", t);
            MessageDialog.openError(getShell(), "Error: " + t.getClass().getName(), t.getMessage());
            return false;
        }
        return true;
    }

    private void generate(IProject project, LanguageProjectGenerator.Input.Builder inputBuilder, IProgressMonitor monitor) throws IOException, CoreException {
        final ResourcePath rootDirectory = new EclipseResourcePath(project);

        // Add Spoofax LWB nature, which also adds the Java nature.
        SpoofaxLwbNature.addTo(project, null);

        // Generate language project.
        languageProjectGenerator.generate(inputBuilder.rootDirectory(rootDirectory).build());

        // Configure JDT.
        // TODO: sync these hardcoded paths with the compiler
        final IFolder src = getFolder(project, "src", monitor);
        final IFolder srcMain = getFolder(src, "main", monitor);
        final IFolder srcMainJava = getFolder(srcMain, "java", monitor);
        final IFolder build = getDerivedFolder(project, "build", monitor);
        final IFolder buildEclipseClasses = getDerivedFolder(build, "eclipseclasses", monitor);
        final IFolder buildGenerated = getDerivedFolder(build, "generated", monitor);
        final IFolder buildGeneratedSources = getDerivedFolder(buildGenerated, "sources", monitor);
        final IFolder buildGeneratedSourcesLanguage = getDerivedFolder(buildGeneratedSources, "language", monitor);
        final IFolder buildGeneratedSourcesAdapter = getDerivedFolder(buildGeneratedSources, "adapter", monitor);
        final IFolder buildGeneratedSourcesEclipse = getDerivedFolder(buildGeneratedSources, "eclipse", monitor);
        final IFolder buildGeneratedSourcesLanguageSpecification = getDerivedFolder(buildGeneratedSources, "languageSpecification", monitor);
        final IFolder buildGeneratedSourcesLanguageSpecificationJava = getDerivedFolder(buildGeneratedSourcesLanguageSpecification, "java", monitor);
        final IFolder buildGeneratedSourcesAnnotationProcessor = getDerivedFolder(buildGeneratedSources, "annotationProcessor", monitor);
        final IFolder buildGeneratedSourcesAnnotationProcessorJava = getDerivedFolder(buildGeneratedSourcesAnnotationProcessor, "java", monitor);
        final IFolder buildGeneratedSourcesAnnotationProcessorJavaMain = getDerivedFolder(buildGeneratedSourcesAnnotationProcessorJava, "main", monitor);

        final IPath[] emptyPaths = new IPath[]{};
        final IClasspathAttribute optionalAttribute = JavaCore.newClasspathAttribute(IClasspathAttribute.OPTIONAL, "true");
        final IClasspathAttribute[] sourceAttributes = new IClasspathAttribute[]{optionalAttribute};
        final IClasspathAttribute ignoreOptionalProblemsAttribute = JavaCore.newClasspathAttribute(IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS, "true");
        final IClasspathAttribute[] derivedSourceAttributes = new IClasspathAttribute[]{optionalAttribute, ignoreOptionalProblemsAttribute};

        final ArrayList<IClasspathEntry> classpathEntries = new ArrayList<>();
        classpathEntries.add(JavaCore.newSourceEntry(srcMainJava.getFullPath(), emptyPaths, emptyPaths, null, sourceAttributes));
        classpathEntries.add(JavaCore.newSourceEntry(buildGeneratedSourcesLanguage.getFullPath(), emptyPaths, emptyPaths, null, derivedSourceAttributes));
        classpathEntries.add(JavaCore.newSourceEntry(buildGeneratedSourcesAdapter.getFullPath(), emptyPaths, emptyPaths, null, derivedSourceAttributes));
        classpathEntries.add(JavaCore.newSourceEntry(buildGeneratedSourcesEclipse.getFullPath(), emptyPaths, emptyPaths, null, derivedSourceAttributes));
        classpathEntries.add(JavaCore.newSourceEntry(buildGeneratedSourcesLanguageSpecificationJava.getFullPath(), emptyPaths, emptyPaths, null, derivedSourceAttributes));
        classpathEntries.add(JavaCore.newSourceEntry(buildGeneratedSourcesAnnotationProcessorJavaMain.getFullPath(), emptyPaths, emptyPaths, null, derivedSourceAttributes));

        final List<File> classPath = ClassPathUtil.getClassPath();
        for(File classPathEntry : classPath) {
            classpathEntries.add(JavaCore.newLibraryEntry(Path.fromOSString(classPathEntry.getAbsolutePath()), null, null));
        }

        classpathEntries.add(JavaCore.newContainerEntry(Path.fromOSString("org.eclipse.jdt.launching.JRE_CONTAINER")));

        final IJavaProject javaProject = JavaCore.create(project);
        javaProject.setOutputLocation(buildEclipseClasses.getFullPath(), monitor);
        javaProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[0]), monitor);

        project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
    }


    private IProject getProject(String name) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }

    private void createProject(IProject project, @Nullable IPath projectPath) throws CoreException {
        if(projectPath != null) {
            final String projectId = project.getName();
            final IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(projectId);
            projectPath = projectPath.append(projectId);
            description.setLocation(projectPath);
            project.create(description, null);
        } else {
            project.create(null);
        }
        project.open(null);
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
    }

    private IFolder getFolder(IContainer parent, String path, @Nullable IProgressMonitor monitor) throws CoreException {
        final IFolder folder = parent.getFolder(Path.fromOSString(path));
        if(!folder.exists()) {
            folder.create(true, true, monitor);
        }
        return folder;
    }

    private IFolder getDerivedFolder(IContainer parent, String path, @Nullable IProgressMonitor monitor) throws CoreException {
        final IFolder folder = getFolder(parent, path, monitor);
        folder.setDerived(true, monitor);
        return folder;
    }
}
