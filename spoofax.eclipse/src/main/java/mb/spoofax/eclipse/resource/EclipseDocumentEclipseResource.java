package mb.spoofax.eclipse.resource;

import mb.resource.ReadableResource;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class EclipseDocumentEclipseResource<D extends IDocument & IDocumentExtension4> implements Resource, ReadableResource, WrapsEclipseResource {
    private final String portablePathString;
    private final D document;
    private final @Nullable IFile file;


    public EclipseDocumentEclipseResource(String portablePathString, D document, @Nullable IFile file) {
        this.portablePathString = portablePathString;
        this.document = document;
        this.file = file;
    }

    @Override public void close() throws IOException {
        // Nothing to close.
    }

    @Override public boolean exists() {
        return true;
    }

    @Override public boolean isReadable() {
        return true;
    }

    @Override public Instant getLastModifiedTime() {
        final long stamp = document.getModificationStamp();
        if(stamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) {
            return Instant.MIN;
        }
        return Instant.ofEpochMilli(stamp);
    }

    @Override public long getSize() {
        return document.getLength() * 2; // Java Strings are UTF-16: 2 bytes per character.
    }

    @Override public InputStream newInputStream() {
        return new ByteArrayInputStream(document.get().getBytes(StandardCharsets.UTF_8));
    }

    @Override public byte[] readBytes() {
        return document.get().getBytes(StandardCharsets.UTF_8);
    }

    @Override public String readString(Charset fromBytesCharset) {
        return document.get();
    }


    @Override public ResourceKey getKey() {
        return new EclipseResourceKey(portablePathString);
    }


    @Override public @Nullable IResource getWrappedEclipseResource() {
        return file;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final EclipseDocumentEclipseResource that = (EclipseDocumentEclipseResource) o;
        return portablePathString.equals(that.portablePathString);
    }

    @Override public int hashCode() {
        return portablePathString.hashCode();
    }

    @Override public String toString() {
        return "Document@" + portablePathString;
    }
}
