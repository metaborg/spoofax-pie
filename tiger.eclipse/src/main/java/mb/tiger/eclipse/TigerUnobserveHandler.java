package mb.tiger.eclipse;

import mb.spoofax.eclipse.command.UnobserveHandler;

public class TigerUnobserveHandler extends UnobserveHandler {
    public TigerUnobserveHandler() {
        super(TigerPlugin.getComponent());
    }
}
