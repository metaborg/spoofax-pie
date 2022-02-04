package mb.spoofax.lwb.eclipse.generator;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.eclipse.SpoofaxPlugin;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.spoofax.eclipse.util.StatusUtil;
import mb.spoofax.lwb.compiler.generator.LanguageProjectGenerator;
import mb.spoofax.lwb.eclipse.SpoofaxLwbParticipant;
import mb.spoofax.lwb.eclipse.SpoofaxLwbNature;
import mb.spoofax.lwb.eclipse.util.JavaProjectUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class LanguageProjectWizard extends Wizard implements INewWizard {
    private final Logger logger;
    private final LanguageProjectWizardPage page;
    private final LanguageProjectGenerator languageProjectGenerator;

    public LanguageProjectWizard() {
        final LoggerFactory loggerFactory = SpoofaxPlugin.getLoggerComponent().getLoggerFactory();
        this.logger = loggerFactory.create(getClass());
        this.page = new LanguageProjectWizardPage(loggerFactory);
        this.languageProjectGenerator = SpoofaxLwbParticipant.getInstance().getSpoofax3Compiler().component.getLanguageProjectGenerator();
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
        SpoofaxLwbNature.addTo(project, null);
        languageProjectGenerator.generate(inputBuilder.rootDirectory(rootDirectory).build());
        JavaProjectUtil.configureProject(project, monitor);
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
}
