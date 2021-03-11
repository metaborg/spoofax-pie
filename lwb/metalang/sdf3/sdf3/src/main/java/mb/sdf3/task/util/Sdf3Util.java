package mb.sdf3.task.util;

import mb.common.result.Result;
import mb.pie.api.Supplier;
import mb.pie.api.ValueSupplier;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.resource.hierarchical.walk.TrueResourceWalker;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;

public class Sdf3Util {
    public static ResourceWalker createResourceWalker() {
        return new TrueResourceWalker();
    }

    public static ResourceMatcher createResourceMatcher() {
        return new PathResourceMatcher(new ExtensionsPathMatcher("tmpl", "sdf3"));
    }
}
