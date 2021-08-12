package mb.tiger.eclipse;

import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.editor.SpoofaxSourceViewerConfiguration;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class TigerEditor extends SpoofaxEditor {
    public TigerEditor() {
        super(TigerLanguage.getInstance().getComponent(), TigerLanguage.getInstance().getPieComponent());
    }

    @Override protected SourceViewerConfiguration createSourceViewerConfiguration() {
        return new SpoofaxSourceViewerConfiguration(
            this,
            TigerLanguage.getInstance().getComponent(),
            TigerLanguage.getInstance().getPieComponent()
        );
    }
}
