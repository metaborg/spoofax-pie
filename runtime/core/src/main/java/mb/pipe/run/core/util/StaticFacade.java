package mb.pipe.run.core.util;

import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.meta.core.SpoofaxMeta;

public class StaticFacade {
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
