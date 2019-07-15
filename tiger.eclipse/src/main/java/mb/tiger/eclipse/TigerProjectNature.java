package mb.tiger.eclipse;

import mb.spoofax.eclipse.nature.SpoofaxProjectNature;

public class TigerProjectNature extends SpoofaxProjectNature {
    public static final String id = TigerPlugin.pluginId + ".nature";

    public TigerProjectNature() {
        super(TigerPlugin.getComponent());
    }
}
