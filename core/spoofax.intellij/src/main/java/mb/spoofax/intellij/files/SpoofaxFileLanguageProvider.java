package mb.spoofax.intellij.files;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import mb.spoofax.intellij.editor.SpoofaxIntellijFile;
import org.checkerframework.checker.nullness.qual.Nullable;

// org.jetbrains.kotlin.idea.scratch.ScratchFileLanguageProvider


/**
 * A Spoofax file language provider.
 */
public abstract class SpoofaxFileLanguageProvider {

    /**
     * Extension point for Spoofax file language providers.
     */
    private static final LanguageExtension<SpoofaxFileLanguageProvider> EXTENSION
        = new LanguageExtension<>("mb.spoofax.intellij.files.spoofaxFileLanguageProvider");

    /**
     * Gets the {@link SpoofaxFileLanguageProvider} for the specified language.
     * @param language The language.
     * @return The language provider; or {@code null} when not found.
     */
    @Nullable public static SpoofaxFileLanguageProvider get(Language language) {
        return EXTENSION.forLanguage(language);
    }

    /**
     * Gets the {@link SpoofaxFileLanguageProvider} for the specified file type.
     * @param fileType The file type.
     * @return The language provider; or {@code null} when not found.
     */
    @Nullable public static SpoofaxFileLanguageProvider get(FileType fileType) {
        if (!(fileType instanceof LanguageFileType)) return null;
        Language language = ((LanguageFileType)fileType).getLanguage();
        return get(language);
    }

    @Nullable
    public SpoofaxIntellijFile newSpoofaxFile(Project project, VirtualFile file) {
        @Nullable SpoofaxIntellijFile spoofaxFile = createSpoofaxFile(project, file);
        if (spoofaxFile == null) return null;
        // TODO: Attach handlers
        return spoofaxFile;
    }

    @Nullable
    protected abstract SpoofaxIntellijFile createSpoofaxFile(Project project, VirtualFile file);

}
