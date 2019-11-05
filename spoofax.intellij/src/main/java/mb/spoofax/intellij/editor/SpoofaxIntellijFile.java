package mb.spoofax.intellij.editor;

// https://github.com/JetBrains/kotlin/blob/f419d2eb30e848e42c279d27300a9c164e72811f/idea/idea-jvm/src/org/jetbrains/kotlin/idea/scratch/ScratchFile.kt

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;


public class SpoofaxIntellijFile {

    private final Project project;
    private final VirtualFile virtualFile;

    public SpoofaxIntellijFile(Project project, VirtualFile virtualFile) {
        this.project = project;
        this.virtualFile = virtualFile;
    }

    public Project getProject() {
        return project;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

}
