package mb.tiger.intellij;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TigerFileType extends LanguageFileType {
    static final TigerFileType instance = new TigerFileType();
    static final String extension = "tig";

    private TigerFileType() {
        super(TigerLanguage.instance);
    }

    @Override public String getName() {
        return "Tiger";
    }

    @Override public String getDescription() {
        return "Tiger file";
    }

    @Override public String getDefaultExtension() {
        return extension;
    }

    @Override public @Nullable Icon getIcon() {
        return null;
    }
}
