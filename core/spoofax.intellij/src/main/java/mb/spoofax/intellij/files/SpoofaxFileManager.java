package mb.spoofax.intellij.files;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import mb.spoofax.intellij.editor.SpoofaxIntellijFile;
import org.checkerframework.checker.nullness.qual.Nullable;


public interface SpoofaxFileManager {
    @Nullable
    SpoofaxIntellijFile tryCreate(Project project, VirtualFile virtualFile);
}
