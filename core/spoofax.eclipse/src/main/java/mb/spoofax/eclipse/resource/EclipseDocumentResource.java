package mb.spoofax.eclipse.resource;

import mb.resource.ReadableResource;
import mb.resource.Resource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class EclipseDocumentResource implements ReadableResource, WrapsEclipseResource {
    private final IDocument document;
    private final EclipseDocumentKey key;
    private final @Nullable EclipseResource file;


    public EclipseDocumentResource(IDocument document, EclipseDocumentKey key) {
        this.document = document;
        this.key = key;
        this.file = null;
    }

    public EclipseDocumentResource(IDocument document, String id) {
        this(document, new EclipseDocumentKey(id));
    }

    public EclipseDocumentResource(IDocument document, IFile file, EclipseResourceRegistry resourceRegistry) {
        this.document = document;
        this.key = new EclipseDocumentKey(file);
        this.file = new EclipseResource(resourceRegistry, file);
    }

    @Override public void close() {
        if(file != null) {
            file.close();
        }
    }


    @Override public boolean exists() {
        return true;
    }

    @Override public boolean isReadable() {
        return true;
    }

    @Override public Instant getLastModifiedTime() {
        if(document instanceof IDocumentExtension4) {
            final IDocumentExtension4 documentExtension = (IDocumentExtension4)document;
            final long stamp = documentExtension.getModificationStamp();
            if(stamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP) {
                return Instant.MIN;
            }
            return Instant.ofEpochMilli(stamp);
        } else {
            return Instant.MAX;
        }
    }

    @Override public long getSize() {
        return document.getLength() * 2; // Java Strings are UTF-16: 2 bytes per character.
    }

    @Override public InputStream openRead() {
        return new ByteArrayInputStream(document.get().getBytes(StandardCharsets.UTF_8));
    }

    @Override public byte[] readBytes() {
        return document.get().getBytes(StandardCharsets.UTF_8);
    }

    @Override public String readString(Charset fromCharset) {
        // Ignore the character set, we do not need to decode from bytes.
        return document.get();
    }


    @Override public EclipseDocumentKey getKey() {
        return key;
    }


    public IDocument getDocument() {
        return document;
    }

    public @Nullable EclipseResource getFile() {
        return file;
    }

    @Override public @Nullable IResource getWrappedEclipseResource() {
        if(file != null) {
            return file.getWrappedEclipseResource();
        }
        return null;
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final EclipseDocumentResource other = (EclipseDocumentResource)obj;
        return key.equals(other.key);
    }

    @Override public int hashCode() {
        return key.hashCode();
    }

    @Override public String toString() {
        return key.toString();
    }
}
