package mb.tiger.intellij;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TigerFileType extends LanguageFileType {
    protected TigerFileType() {
        super(new TigerLanguage());
    }

    @Override public String getName() {
        return "Tiger";
    }

    @Override public String getDescription() {
        return "Tiger";
    }

    @Override public String getDefaultExtension() {
        return "tig";
    }

    @Override public @Nullable Icon getIcon() {
        return null;
    }
}
