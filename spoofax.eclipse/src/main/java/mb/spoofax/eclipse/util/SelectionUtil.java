package mb.spoofax.eclipse.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Utility functions for selections.
 */
public class SelectionUtil {
    /**
     * Returns the selection of the active part of the active workbench window.
     *
     * @return Active selection, or null if there is no active selection.
     */
    public static @Nullable ISelection getActiveSelection() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart().getSite().getSelectionProvider().getSelection();
    }


    /**
     * Converts given selection into a structured selection.
     *
     * @param selection Selection to convert.
     * @return Structured selection, or null if given selection is not a structured selection.
     */
    public static @Nullable IStructuredSelection toStructured(ISelection selection) {
        if(selection instanceof IStructuredSelection) {
            return (IStructuredSelection) selection;
        }
        return null;
    }


    /**
     * Attempts to retrieve a single resource from given selection.
     *
     * @param selection Structured selection to search.
     * @return Selected resource, or null if it could not be retrieved.
     */
    public static @Nullable IResource toResource(IStructuredSelection selection) {
        final @Nullable Object selected = selection.getFirstElement();
        if(selected == null) {
            return null;
        }
        return elementToResource(selected);
    }

    /**
     * Retrieves all resources from given selection.
     *
     * @param selection Structured selection to search.
     * @return Selected resources
     */
    public static ArrayList<IResource> toResources(IStructuredSelection selection) {
        final ArrayList<IResource> resources = new ArrayList<>();
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext(); ) {
            final Object selected = iterator.next();
            final @Nullable IResource resource = elementToResource(selected);
            if(resource != null) {
                resources.add(resource);
            }
        }
        return resources;
    }

    /**
     * Attempts to convert given selection element to a resource.
     *
     * @param selected Selected element to convert.
     * @return Resource converted from selected element, or null if no resource could be converted.
     */
    @SuppressWarnings("cast") public static @Nullable IResource elementToResource(Object selected) {
        if(selected instanceof IProjectNature) {
            // Test for resource nature as well, in the Package explorer, Java resources of class JavaProject,
            // which implements IProjectNature.
            final IProjectNature nature = (IProjectNature) selected;
            return nature.getProject();
        } else if(selected instanceof IResource) {
            return (IResource) selected;
        } else if(selected instanceof IAdaptable) {
            final IAdaptable adaptable = (IAdaptable) selected;
            return (IResource) adaptable.getAdapter(IResource.class);
        } else {
            return null;
        }
    }


    /**
     * Attempts to retrieve a single file from given selection.
     *
     * @param selection Structured selection to search.
     * @return Selected file, or null if it could not be retrieved.
     */
    public static @Nullable IFile toFile(IStructuredSelection selection) {
        final @Nullable Object selected = selection.getFirstElement();
        if(selected == null) {
            return null;
        }
        return elementToFile(selected);
    }

    /**
     * Retrieves all files from given selection.
     *
     * @param selection Structured selection to search.
     * @return Selected files
     */
    public static ArrayList<IFile> toFiles(IStructuredSelection selection) {
        final ArrayList<IFile> files = new ArrayList<>();
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext(); ) {
            final Object selected = iterator.next();
            final @Nullable IFile file = elementToFile(selected);
            if(file != null) {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * Attempts to convert given selection element to a file.
     *
     * @param selected Selected element to convert.
     * @return File converted from selected element, or null if no file could be converted.
     */
    @SuppressWarnings("cast") public static @Nullable IFile elementToFile(Object selected) {
        if(selected instanceof IFile) {
            return (IFile) selected;
        } else if(selected instanceof IAdaptable) {
            final IAdaptable adaptable = (IAdaptable) selected;
            // COMPAT: DO NOT REMOVE CAST, it is required for older versions of Eclipse.
            @SuppressWarnings("RedundantCast") final IFile file = (IFile) adaptable.getAdapter(IFile.class);
            return file;
        } else {
            return null;
        }
    }


    /**
     * Attempts to retrieve a single project from given selection.
     *
     * @param selection Structured selection to search.
     * @return Selected project, or null if it could not be retrieved.
     */
    public static @Nullable IProject toProject(IStructuredSelection selection) {
        final @Nullable Object selected = selection.getFirstElement();
        if(selected == null) {
            return null;
        }
        return elementToProject(selected);
    }

    /**
     * Retrieves all projects from given selection.
     *
     * @param selection Structured selection to search.
     * @return Selected projects
     */
    public static ArrayList<IProject> toProjects(IStructuredSelection selection) {
        final ArrayList<IProject> projects = new ArrayList<>();
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext(); ) {
            final Object selected = iterator.next();
            final @Nullable IProject project = elementToProject(selected);
            if(project != null) {
                projects.add(project);
            }
        }
        return projects;
    }

    /**
     * Attempts to convert given selection element to a project.
     *
     * @param selected Selected element to convert.
     * @return Project converted from selected element, or null if no project could be converted.
     */
    public static @Nullable IProject elementToProject(Object selected) {
        if(selected instanceof IProjectNature) {
            // Test for project nature as well. In the Package explorer, Java projects are of class JavaProject, which implement IProjectNature.
            final IProjectNature nature = (IProjectNature) selected;
            return nature.getProject();
        } else if(selected instanceof IProject) {
            return (IProject) selected;
        } else if(selected instanceof IAdaptable) {
            final IAdaptable adaptable = (IAdaptable) selected;
            // COMPAT: DO NOT REMOVE CAST, it is required for older versions of Eclipse.
            @SuppressWarnings("RedundantCast") final IProject project = (IProject) adaptable.getAdapter(IProject.class);
            return project;
        } else {
            return null;
        }
    }
}
