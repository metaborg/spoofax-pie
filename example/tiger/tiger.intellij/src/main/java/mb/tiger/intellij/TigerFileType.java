package mb.tiger.intellij;

import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.intellij.IntellijLanguageFileType;

import javax.inject.Inject;

@LanguageScope
public class TigerFileType extends IntellijLanguageFileType {
    @Inject protected TigerFileType() {
        super(TigerPlugin.getComponent());
    }
}
