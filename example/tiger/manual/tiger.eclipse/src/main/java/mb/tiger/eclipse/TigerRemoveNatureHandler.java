package mb.tiger.eclipse;

import mb.spoofax.eclipse.nature.RemoveNatureHandler;

public class TigerRemoveNatureHandler extends RemoveNatureHandler {
    public TigerRemoveNatureHandler() {
        super(TigerLanguage.getInstance().getComponent());
    }
}
