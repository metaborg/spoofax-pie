package mb.tiger.intellij.editor;

import mb.spoofax.intellij.SpoofaxPlugin;
import mb.spoofax.intellij.editor.SpoofaxCompletionContributor;
import mb.tiger.intellij.TigerPlugin;

public class TigerCompletionContributor extends SpoofaxCompletionContributor {
    // Instantiated by IntelliJ.
    protected TigerCompletionContributor() {
        super(TigerPlugin.getComponent(), TigerPlugin.getPieComponent(), SpoofaxPlugin.getLoggerComponent().getLoggerFactory());
    }
}
