package mb.tiger.intellij;

import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.intellij.IntellijLanguage;

import javax.inject.Inject;

@LanguageScope
public class TigerLanguage extends IntellijLanguage {
    @Inject public TigerLanguage() {
        super(TigerPlugin.getComponent());
    }
}
