package mb.tiger.intellij;

import mb.spoofax.intellij.IntellijLanguage;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;

@TigerScope
public class TigerLanguage extends IntellijLanguage {
    @Inject public TigerLanguage() {
        super(TigerPlugin.getComponent());
    }
}
