package mb.str.spoofax.incr;

import mb.resource.hierarchical.ResourcePath;
import mb.str.spoofax.StrategoScope;
import mb.stratego.build.strincr.Backend;

import javax.inject.Inject;

@StrategoScope
public class AsStringResourcePathConverter implements Backend.ResourcePathConverter {
    @Inject public AsStringResourcePathConverter() {}

    @Override public String toString(ResourcePath resourcePath) {
        return resourcePath.asString();
    }
}
