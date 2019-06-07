package mb.spoofax.intellij.resource;

import mb.resource.ResourceKey;

public class IntellijResourceKey implements ResourceKey {
    final String url;


    public IntellijResourceKey(String url) {
        this.url = url;
    }


    @Override public String getQualifier() {
        return IntellijResourceRegistry.qualifier;
    }

    @Override public String getId() {
        return url;
    }

    @Override public String getIdStringRepresentation() {
        return url;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final IntellijResourceKey that = (IntellijResourceKey) o;
        return url.equals(that.url);
    }

    @Override public int hashCode() {
        return url.hashCode();
    }

    @Override public String toString() {
        return ResourceKey.toStringRepresentation(this);
    }
}
