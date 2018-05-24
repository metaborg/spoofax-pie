package mb.spoofax.runtime.impl.legacy;

import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.SpoofaxMeta;

public class StaticSpoofaxCoreFacade {
    private static SpoofaxMeta facade;


    public static void init(SpoofaxMeta spoofaxMeta) {
        facade = spoofaxMeta;
    }


    public static Spoofax spoofax() {
        return facade.parent;
    }

    public static SpoofaxMeta spoofaxMeta() {
        return facade;
    }
}
