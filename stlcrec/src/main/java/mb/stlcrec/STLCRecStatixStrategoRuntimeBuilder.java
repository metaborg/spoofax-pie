package mb.stlcrec;

import mb.nabl2.common.NaBL2PrimitiveLibrary;
import mb.resource.url.URLPath;
import mb.statix.common.StatixPrimitiveLibrary;
import mb.stratego.common.StrategoRuntimeBuilder;

import java.net.URL;

public class STLCRecStatixStrategoRuntimeBuilder {
    public static StrategoRuntimeBuilder fromClassLoaderResources() {
        final StrategoRuntimeBuilder builder = STLCRecStrategoRuntimeBuilder.fromClassLoaderResources();
        final URL locationURL = STLCRecStrategoRuntimeBuilder.class.getProtectionDomain().getCodeSource().getLocation();
        final URLPath path = new URLPath(locationURL);
        final URLPath correctedPath = path.appendRelativePath("mb/stlcrec");
        builder.addLibrary(new NaBL2PrimitiveLibrary());
        builder.addLibrary(new StatixPrimitiveLibrary("org.metaborg", "stlcrec", "0.1.0", correctedPath.toString()));
        return builder;
    }
}
