package mb.tiger.eclipse;

import mb.spoofax.eclipse.build.SpoofaxProjectBuilder;

public class TigerProjectBuilder extends SpoofaxProjectBuilder {
    public static final String id = TigerPlugin.pluginId + ".builder";

    public TigerProjectBuilder() {
        super(TigerLanguage.getInstance().getComponent());
    }
}
