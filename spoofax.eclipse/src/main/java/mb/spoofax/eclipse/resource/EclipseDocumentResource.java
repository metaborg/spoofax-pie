package mb.spoofax.eclipse.resource;

import mb.resource.ReadableResource;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class EclipseDocumentResource<D extends IDocument & IDocumentExtension4> implements Resource, ReadableResource {
    private final String portablePathString;
    private final D document;


    public EclipseDocumentResource(String portablePathString, D document) {
        this.portablePathString = portablePathString;
        this.document = document;
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


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final EclipseDocumentResource that = (EclipseDocumentResource) o;
        return portablePathString.equals(that.portablePathString);
    }

    @Override public int hashCode() {
        return portablePathString.hashCode();
    }

    @Override public String toString() {
        return "Document@" + portablePathString;
    }
}
