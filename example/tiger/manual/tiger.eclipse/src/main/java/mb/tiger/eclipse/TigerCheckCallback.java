package mb.tiger.eclipse;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import mb.spoofax.eclipse.EclipseIdentifiers;
import mb.spoofax.eclipse.editor.CheckCallback;
import mb.spoofax.eclipse.resource.EclipseResourcePath;
import mb.tiger.spoofax.TigerParticipant;

public class TigerCheckCallback extends CheckCallback {
    @AssistedFactory public interface Factory extends CheckCallback.Factory {
        TigerCheckCallback create(EclipseResourcePath file);
    }

    @AssistedInject public TigerCheckCallback(@Assisted EclipseResourcePath file) {
        super(file);
    }

    @Override protected EclipseIdentifiers getEclipseIdentifiers() {
        return TigerEclipseParticipantFactory.getParticipant().getComponent().getEclipseIdentifiers();
    }
}
