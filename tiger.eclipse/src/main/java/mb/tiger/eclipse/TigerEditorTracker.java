package mb.tiger.eclipse;

import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.editor.EditorTracker;

import javax.inject.Inject;

@LanguageScope
public class TigerEditorTracker extends EditorTracker {
    @Inject public TigerEditorTracker(EclipseIdentifiers eclipseIdentifiers) {
        super(eclipseIdentifiers);
    }
}
