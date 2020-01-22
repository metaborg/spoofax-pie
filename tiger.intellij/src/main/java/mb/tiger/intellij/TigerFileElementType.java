package mb.tiger.intellij;

import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.intellij.IntellijFileElementType;

import javax.inject.Inject;

@LanguageScope
public class TigerFileElementType extends IntellijFileElementType {
    @Inject public TigerFileElementType() {
        super(TigerPlugin.getComponent());
    }
}
