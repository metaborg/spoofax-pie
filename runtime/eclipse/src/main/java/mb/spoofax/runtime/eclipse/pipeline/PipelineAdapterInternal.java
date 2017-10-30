package mb.spoofax.runtime.eclipse.pipeline;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;


public interface PipelineAdapterInternal extends PipelineAdapter {
    void scan() throws CoreException;


    void addProject(IProject project);

    void removeProject(IProject project);


    void addEditor(SpoofaxEditor editor);

    void removeEditor(SpoofaxEditor editor);
}
