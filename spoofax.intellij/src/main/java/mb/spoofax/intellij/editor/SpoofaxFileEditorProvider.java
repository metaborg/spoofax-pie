package mb.spoofax.intellij.editor;

import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import mb.spoofax.intellij.files.ISpoofaxFileManager;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;


/**
 * File editor provider for a Spoofax language.
 */
public abstract class SpoofaxFileEditorProvider implements FileEditorProvider, DumbAware {

    private static final String SPOOFAX_FILEEDITOR_PROVIDER = SpoofaxFileEditorProvider.class.getName();

    private final SpoofaxEditor.Factory spoofaxEditorFactory;
    private final ISpoofaxFileManager spoofaxFileManager;

    public SpoofaxFileEditorProvider(SpoofaxEditor.Factory spoofaxEditorFactory, ISpoofaxFileManager spoofaxFileManager) {
        this.spoofaxEditorFactory = spoofaxEditorFactory;
        this.spoofaxFileManager = spoofaxFileManager;
    }

    //    @Override
//    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
//        return false;
//    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        @Nullable SpoofaxIntellijFile spoofaxFile = this.spoofaxFileManager.tryCreate(project, file);
        if (spoofaxFile != null) {
            return this.spoofaxEditorFactory.create(spoofaxFile);
        } else {
            return TextEditorProvider.getInstance().createEditor(project, file);
        }
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return SPOOFAX_FILEEDITOR_PROVIDER;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        // Don't create IntelliJ's default editor.
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

}
