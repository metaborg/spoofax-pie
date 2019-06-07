package mb.stlcrec;

import mb.resource.url.URLResource;
import mb.statix.common.StatixPrimitiveLibrary;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URL;

public class STLCRecStatixStrategoRuntimeBuilder  {
    public static StrategoRuntimeBuilder fromClassLoaderResources() {
        final StrategoRuntimeBuilder builder = STLCRecStrategoRuntimeBuilder.fromClassLoaderResources();
        builder.addLibrary(new StatixPrimitiveLibrary("bla", "di", "Bla", "doo"));
        return builder;
    }
}
