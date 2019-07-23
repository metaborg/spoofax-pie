package mb.spoofax.eclipse.editor;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.runtime.IPath;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TextStorage implements IEncodedStorage {
    private final String name;

    TextStorage(String name) {
        this.name = name;
    }

    @Override public InputStream getContents() {
        return new ByteArrayInputStream(TextDocumentProvider.getText(name).getBytes(StandardCharsets.UTF_8));
    }

    @Override public String getCharset() {
        return "UTF-8";
    }

    @Override public @Nullable IPath getFullPath() {
        return null;
    }

    @Override public String getName() {
        return name;
    }

    @Override public boolean isReadOnly() {
        return true;
    }

    @Override public <T> @Nullable T getAdapter(Class<T> adapter) {
        return null;
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final TextStorage other = (TextStorage) obj;
        return name.equals(other.name);
    }

    @Override public int hashCode() {
        return Objects.hash(name);
    }

    @Override public String toString() {
        return name;
    }
}
