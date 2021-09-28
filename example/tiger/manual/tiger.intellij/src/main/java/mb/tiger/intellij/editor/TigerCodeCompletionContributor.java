package mb.tiger.intellij.editor;

import mb.spoofax.intellij.editor.SpoofaxCodeCompletionContributor;
import mb.tiger.intellij.TigerPlugin;

public class TigerCodeCompletionContributor extends SpoofaxCodeCompletionContributor {
    // Instantiated by IntelliJ.
    protected TigerCodeCompletionContributor() {
        super(TigerPlugin.getComponent(), TigerPlugin.getPieComponent());
    }
}
