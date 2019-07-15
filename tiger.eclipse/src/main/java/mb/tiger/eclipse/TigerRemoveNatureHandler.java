package mb.tiger.eclipse;

import mb.spoofax.eclipse.command.RemoveNatureHandler;

public class TigerRemoveNatureHandler extends RemoveNatureHandler {
    public TigerRemoveNatureHandler() {
        super(TigerPlugin.getComponent());
    }
}
