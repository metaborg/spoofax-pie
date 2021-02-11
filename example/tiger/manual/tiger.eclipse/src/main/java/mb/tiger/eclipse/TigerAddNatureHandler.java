package mb.tiger.eclipse;

import mb.spoofax.eclipse.nature.AddNatureHandler;

public class TigerAddNatureHandler extends AddNatureHandler {
    public TigerAddNatureHandler() {
        super(TigerLanguage.getInstance().getComponent());
    }
}
