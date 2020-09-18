package mb.tiger.intellij;

import mb.spoofax.intellij.IntellijLanguageFileType;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;

@TigerScope
public class TigerFileType extends IntellijLanguageFileType {
    @Inject protected TigerFileType() {
        super(TigerPlugin.getComponent());
    }
}
