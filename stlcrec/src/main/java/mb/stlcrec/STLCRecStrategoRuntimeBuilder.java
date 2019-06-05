package mb.stlcrec;

import mb.resource.url.URLResource;
import mb.stratego.common.StrategoRuntimeBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URL;

public class STLCRecStrategoRuntimeBuilder extends StrategoRuntimeBuilder {
    public static STLCRecStrategoRuntimeBuilder fromClassLoaderResources() {
        final String ctreeResource = "mb/stlcrec/stratego.ctree";
        final @Nullable URL ctree = STLCRecStrategoRuntimeBuilder.class.getClassLoader().getResource(ctreeResource);
        if(ctree == null) {
            throw new RuntimeException(
                "Cannot create Tiger Stratego runtime; cannot find resource '" + ctreeResource + "' in classloader resources");
        }

        final STLCRecStrategoRuntimeBuilder builder = new STLCRecStrategoRuntimeBuilder();
        builder.addCtree(new URLResource(ctree));
        builder.withJarParentClassLoader(STLCRecStrategoRuntimeBuilder.class.getClassLoader());
        return builder;
    }
}
