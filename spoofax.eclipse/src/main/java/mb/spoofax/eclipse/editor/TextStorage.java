package mb.spoofax.eclipse.editor;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TextStorage implements IEncodedStorage {
    private final String text;
    private final String name;

    public TextStorage(String text, String name) {
        this.text = text;
        this.name = name;
    }


    @Override public InputStream getContents() throws CoreException {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    @Override public String getCharset() throws CoreException {
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
}
