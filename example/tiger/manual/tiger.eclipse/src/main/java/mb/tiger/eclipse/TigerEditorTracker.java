package mb.tiger.eclipse;

import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.editor.EditorTracker;
import mb.tiger.spoofax.TigerQualifier;
import mb.tiger.spoofax.TigerScope;

import javax.inject.Inject;

@TigerScope
public class TigerEditorTracker extends EditorTracker {
    @Inject public TigerEditorTracker(@TigerQualifier EclipseIdentifiers eclipseIdentifiers) {
        super(eclipseIdentifiers, TigerEditor.class);
    }
}
