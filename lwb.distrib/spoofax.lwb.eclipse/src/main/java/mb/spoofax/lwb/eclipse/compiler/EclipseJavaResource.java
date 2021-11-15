package mb.spoofax.lwb.eclipse.compiler;

import mb.pie.task.java.jdk.JavaResource;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.eclipse.resource.EclipseResource;

import java.net.URI;

public class EclipseJavaResource extends JavaResource {
    public EclipseJavaResource(HierarchicalResource resource, Kind kind) {
        super(resource, kind);
    }

    public EclipseJavaResource(HierarchicalResource resource) {
        super(resource);
    }


    @Override public URI toUri() {
        // HACK: ECJ uses the path of the URI, which the URI of QualifiedResourceKeyString does not have, which
        //       results in a NPE in ECJ. Special case for local file system resources to return their Java Path URI
        //       so that ECJ works.
        if(resource instanceof FSResource) {
            return ((FSResource)resource).getJavaPath().toUri();
        } else if(resource instanceof EclipseResource) {
            return ((EclipseResource)resource).toLocalFile().toURI();
        }

        return resource.getPath().asResourceKeyString().toUri();
    }

    @Override public String getName() {
        // HACK: ECJ uses this name directly and passes it to new File(...), which does not work for asString.
        //       Special case for local file system resources to return their Path string so that ECJ works.
        if(resource instanceof FSResource) {
            return ((FSResource)resource).getJavaPath().toString();
        } else if(resource instanceof EclipseResource) {
            return ((EclipseResource)resource).toLocalFile().toString();
        }

        return resource.getPath().asString();
    }
}
