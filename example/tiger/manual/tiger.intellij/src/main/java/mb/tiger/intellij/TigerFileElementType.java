package mb.tiger.intellij;

import mb.spoofax.intellij.IntellijFileElementType;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;

@TigerScope
public class TigerFileElementType extends IntellijFileElementType {
    @Inject public TigerFileElementType() {
        super(TigerPlugin.getComponent());
    }
}
