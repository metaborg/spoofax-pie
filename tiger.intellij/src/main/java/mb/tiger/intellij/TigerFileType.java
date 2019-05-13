package mb.tiger.intellij;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import groovy.lang.Singleton;
import mb.spoofax.core.language.LanguageScope;
import org.bouncycastle.jcajce.provider.digest.Tiger;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.swing.*;

public class TigerFileType extends LanguageFileType {

    public static final TigerFileType INSTANCE = new TigerFileType();

    public static final String EXTENSION = "tig";

    // Cannot be instantiated.
    private TigerFileType() {
        super(TigerLanguage.INSTANCE);
    }

    @Override public String getName() {
        return "Tiger";
    }

    @Override public String getDescription() {
        return "Tiger file";
    }

    @Override public String getDefaultExtension() {
        return EXTENSION;
    }

    @Override public @Nullable Icon getIcon() {
        return TigerIcons.FILE;
    }
}
