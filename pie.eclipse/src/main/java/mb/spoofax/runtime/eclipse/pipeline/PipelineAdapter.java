package mb.spoofax.runtime.eclipse.pipeline;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import mb.pie.api.ExecException;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.spoofax.runtime.eclipse.util.Nullable;

public interface PipelineAdapter {
    void addProject(IProject project);

    void buildProject(IProject project, int buildKind, @Nullable IResourceDelta delta,
        @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException, CoreException;

    void removeProject(IProject project);


    void addEditor(SpoofaxEditor editor, String text, IFile file, IProject project);

    void updateEditor(SpoofaxEditor editor, String text, IFile file, IProject project,
        @Nullable IProgressMonitor monitor) throws ExecException, InterruptedException;

    void removeEditor(SpoofaxEditor editor);


    void cleanAll(@Nullable IProgressMonitor monitor) throws CoreException;
}
