package mb.tiger.intellij.editor;

import mb.spoofax.intellij.editor.IntellijCompletionContributor;
import mb.tiger.intellij.TigerPlugin;

public class TigerCompletionContributor extends IntellijCompletionContributor {
    // Instantiated by IntelliJ.
    protected TigerCompletionContributor() {
        super(TigerPlugin.getComponent());
    }
}
