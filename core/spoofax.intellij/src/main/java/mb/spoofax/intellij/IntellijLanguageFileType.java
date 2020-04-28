package mb.spoofax.intellij;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.*;

public class IntellijLanguageFileType extends LanguageFileType {
    private final IntellijLanguageComponent languageComponent;

    protected IntellijLanguageFileType(IntellijLanguageComponent languageComponent) {
        super(languageComponent.getLanguage());
        this.languageComponent = languageComponent;
    }

    @Override public String getName() {
        return languageComponent.getLanguageInstance().getDisplayName();
    }

    @Override public String getDescription() {
        return languageComponent.getLanguageInstance().getDisplayName() + " file";
    }

    @Override public String getDefaultExtension() {
        return languageComponent.getLanguageInstance().getFileExtensions().iterator().next();
    }

    @Override public @Nullable Icon getIcon() {
        return languageComponent.getFileIcon();
    }
}
