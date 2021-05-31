package mb.str.incr;

import mb.resource.hierarchical.ResourcePath;
import mb.str.StrategoScope;
import mb.stratego.build.strincr.ResourcePathConverter;

import javax.inject.Inject;

@StrategoScope
public class AsStringResourcePathConverter implements ResourcePathConverter {
    @Inject public AsStringResourcePathConverter() {}

    @Override public String toString(ResourcePath resourcePath) {
        return resourcePath.asString();
    }
}
