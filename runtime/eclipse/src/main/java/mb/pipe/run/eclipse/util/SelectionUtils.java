package mb.pipe.run.eclipse.util;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.google.common.collect.Lists;

/**
 * Utility functions for selections.
 */
public class SelectionUtils {
    /**
     * Converts given selection into a structured selection.
     * 
     * @param selection
     *            Selection to convert.
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
     * @param selection
     *            Structured selection to search.
     * @return Selected resource, or null if it could not be retrieved.
     */
    public static @Nullable IResource toResource(IStructuredSelection selection) {
        final Object selected = selection.getFirstElement();
        if(selected == null) {
            return null;
        }
        return elementToResource(selected);
    }

    /**
     * Retrieves all resources from given selection.
     * 
     * @param selection
     *            Structured selection to search.
     * @return Selected resources
     */
    public static Iterable<IResource> toResources(IStructuredSelection selection) {
        final Collection<IResource> resources = Lists.newLinkedList();
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            final Object selected = iterator.next();
            final IResource resource = elementToResource(selected);
            if(resource != null) {
                resources.add(resource);
            }
        }
        return resources;
    }

    /**
     * Attempts to convert given selection element to a resource.
     * 
     * @param selected
     *            Selected element to convert.
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
     * @param selection
     *            Structured selection to search.
     * @return Selected file, or null if it could not be retrieved.
     */
    public static @Nullable IFile toFile(IStructuredSelection selection) {
        final Object selected = selection.getFirstElement();
        if(selected == null) {
            return null;
        }
        return elementToFile(selected);
    }

    /**
     * Retrieves all files from given selection.
     * 
     * @param selection
     *            Structured selection to search.
     * @return Selected files
     */
    public static Iterable<IFile> toFiles(IStructuredSelection selection) {
        final Collection<IFile> files = Lists.newLinkedList();
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            final Object selected = iterator.next();
            final IFile file = elementToFile(selected);
            if(file != null) {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * Attempts to convert given selection element to a file.
     * 
     * @param selected
     *            Selected element to convert.
     * @return File converted from selected element, or null if no file could be converted.
     */
    @SuppressWarnings("cast") public static @Nullable IFile elementToFile(Object selected) {
        if(selected instanceof IFile) {
            return (IFile) selected;
        } else if(selected instanceof IAdaptable) {
            final IAdaptable adaptable = (IAdaptable) selected;
            return (IFile) adaptable.getAdapter(IFile.class);
        } else {
            return null;
        }
    }


    /**
     * Attempts to retrieve a single project from given selection.
     * 
     * @param selection
     *            Structured selection to search.
     * @return Selected project, or null if it could not be retrieved.
     */
    public static @Nullable IProject toProject(IStructuredSelection selection) {
        final Object selected = selection.getFirstElement();
        if(selected == null) {
            return null;
        }
        return elementToProject(selected);
    }

    /**
     * Retrieves all projects from given selection.
     * 
     * @param selection
     *            Structured selection to search.
     * @return Selected projects
     */
    public static Iterable<IProject> toProjects(IStructuredSelection selection) {
        final Collection<IProject> projects = Lists.newLinkedList();
        for(Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            final Object selected = iterator.next();
            final IProject project = elementToProject(selected);
            if(project != null) {
                projects.add(project);
            }
        }
        return projects;
    }

    /**
     * Attempts to convert given selection element to a project.
     * 
     * @param selected
     *            Selected element to convert.
     * @return Project converted from selected element, or null if no project could be converted.
     */
    @SuppressWarnings("cast") public static @Nullable IProject elementToProject(Object selected) {
        if(selected instanceof IProjectNature) {
            // Test for project nature as well, in the Package explorer, Java projects of class JavaProject,
            // which implements IProjectNature.
            final IProjectNature nature = (IProjectNature) selected;
            return nature.getProject();
        } else if(selected instanceof IProject) {
            return (IProject) selected;
        } else if(selected instanceof IAdaptable) {
            final IAdaptable adaptable = (IAdaptable) selected;
            return (IProject) adaptable.getAdapter(IProject.class);
        } else {
            return null;
        }
    }
}
