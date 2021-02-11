package mb.tiger.eclipse;

import mb.spoofax.eclipse.menu.UnobserveHandler;

public class TigerUnobserveHandler extends UnobserveHandler {
    public TigerUnobserveHandler() {
        super(TigerLanguage.getInstance().getComponent());
    }
}
