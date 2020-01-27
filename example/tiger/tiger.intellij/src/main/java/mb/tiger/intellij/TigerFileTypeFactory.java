package mb.tiger.intellij;

import mb.spoofax.intellij.IntellijFileTypeFactory;

public class TigerFileTypeFactory extends IntellijFileTypeFactory {
    // Instantiated by IntelliJ.
    private TigerFileTypeFactory() {
        super(TigerPlugin.getComponent());
    }
}
