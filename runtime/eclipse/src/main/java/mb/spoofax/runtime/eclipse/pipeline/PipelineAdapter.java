package mb.spoofax.runtime.eclipse.pipeline;

import java.io.Serializable;
import java.util.ArrayList;

import mb.pie.runtime.core.ObsFuncApp;
import mb.spoofax.runtime.eclipse.editor.SpoofaxEditor;
import mb.vfs.path.PPath;


public interface PipelineAdapter {
    ArrayList<ObsFuncApp<Serializable, Serializable>> workspaceObsFuncApps(WorkspaceUpdate update);

    ObsFuncApp<Serializable, Serializable> projectObsFuncApp(PPath project, WorkspaceUpdate update);

    ObsFuncApp<Serializable, Serializable> editorObsFuncApp(String text, PPath file, PPath project,
        SpoofaxEditor editor, WorkspaceUpdate update);

    ObsFuncApp<Serializable, Serializable> editorObsFuncApp(SpoofaxEditor editor, WorkspaceUpdate update);
}
