package mb.spoofax.intellij.resource;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import mb.resource.ReadableResource;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;


/**
 * An IntelliJ resource.
 */
public final class IntellijResource implements ReadableResource {

    private final VirtualFile file;

    /**
     * Initializes a new instance of the {@link IntellijResource} class.
     *
     * @param file The IntelliJ virtual file representing the resource.
     */
    /* package private */ IntellijResource(VirtualFile file) {
        this.file = file;
    }

    /**
     * Gets the document associated with this resource.
     *
     * @return The associated document; or null when the file has no associated text document
     * (e.g., it is a directory, binary file, or too large).
     */
    @Nullable public Document getDocument() {
        return FileDocumentManager.getInstance().getDocument(this.file);
    }

    /**
     * Gets the text of the document associated with this resource.
     *
     * @return The text of the associated document; or null when the file has no associated text document
     * (e.g., it is a directory, binary file, or too large).
     */
    @Nullable public String getDocumentText() {
        @Nullable Document document = getDocument();
        if (document == null) return null;
        return ReadAction.compute(document::getText);
    }

    @Override public void close() throws IOException {
        // Nothing to close.
    }

    @Override public boolean exists() {
        return this.file.exists();
    }

    @Override public boolean isReadable() {
        return true;
    }

    @Override public Instant getLastModifiedTime() {
        @Nullable Document document = getDocument();
        if (document != null) {  // Happy path
            return Instant.ofEpochMilli(document.getModificationStamp());
        } else { // Unhappy path
            return Instant.ofEpochMilli(this.file.getTimeStamp());
        }
    }

    @Override public long getSize() {
        @Nullable Document document = getDocument();
        if (document != null) { // Happy path
            // NOTE: We represent the IntelliJ text as UTF-16 (TEXT_CHARSET)
            return document.getTextLength() * 2;
        } else { // Unhappy path
            return this.file.getLength();
        }
    }

    @Override
    public InputStream openRead() throws IOException {
        @Nullable String text = getDocumentText();
        if (text != null) {
            return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_16BE));
        } else {
            // NOTE: This will not return the latest changes for a textual file
            return this.file.getInputStream();
        }
    }

    @Override
    public String readString(Charset fromCharset) throws IOException {
        @Nullable String text = getDocumentText();
        if (text != null) { // Happy path
            // Ignore the character set, we do not need to decode from bytes.
            return text;
        } else { // Unhappy path
            return new String(readBytes(), fromCharset);
        }
    }

    @Override public ResourceKey getKey() {
        return new IntellijResourceKey(this.file.getUrl());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final IntellijResource that = (IntellijResource)o;
        return this.file.equals(that.file);
    }

    @Override public int hashCode() {
        return this.file.hashCode();
    }

    @Override public String toString() {
        return this.file.getUrl();
    }
}
