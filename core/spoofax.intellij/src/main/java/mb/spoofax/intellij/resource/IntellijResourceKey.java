package mb.spoofax.intellij.resource;

import com.intellij.openapi.vfs.VirtualFileSystem;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An IntelliJ resource key.
 * <p>
 * IntelliJ resources are uniquely identified by an URL of the form "protocol://path", where the protocol maps to an
 * IntelliJ {@link VirtualFileSystem} and the path is a path in that file system.
 */
public final class IntellijResourceKey implements ResourceKey {

    private final String url;

    /**
     * Initializes a new instance of the {@link IntellijResourceKey} class.
     *
     * @param url The resource URL.
     */
    /* package private */ IntellijResourceKey(String url) {
        this.url = url;
    }


    @Override public String getQualifier() {
        return IntellijResourceRegistry.qualifier;
    }

    @Override public String getId() {
        return url;
    }

    @Override public String getIdAsString() {
        return url;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final IntellijResourceKey that = (IntellijResourceKey)o;
        return url.equals(that.url);
    }

    @Override public int hashCode() {
        return url.hashCode();
    }

    @Override public String toString() {
        return asString();
    }
}
