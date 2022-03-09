package mb.tiger.eclipse;

import mb.spoofax.eclipse.editor.SpoofaxEditor;
import mb.spoofax.eclipse.editor.SpoofaxSourceViewerConfiguration;
import mb.tiger.spoofax.TigerParticipant;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class TigerEditor extends SpoofaxEditor {
    public TigerEditor() {
        super(TigerEclipseParticipantFactory.getParticipant().getComponent(), TigerEclipseParticipantFactory.getParticipant().getPieComponent());
    }

    @Override protected SourceViewerConfiguration createSourceViewerConfiguration() {
        return new SpoofaxSourceViewerConfiguration(
            this,
            TigerEclipseParticipantFactory.getParticipant().getComponent(),
            TigerEclipseParticipantFactory.getParticipant().getPieComponent()
        );
    }
}
