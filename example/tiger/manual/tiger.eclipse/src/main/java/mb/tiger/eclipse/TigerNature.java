package mb.tiger.eclipse;

import mb.spoofax.eclipse.nature.SpoofaxNature;

public class TigerNature extends SpoofaxNature {
    public static final String id = TigerPlugin.pluginId + ".nature";

    public TigerNature() {
        super(TigerLanguage.getInstance().getComponent());
    }
}
