package mb.spt.resource;

import mb.resource.ReadableResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class SptTestCaseResource implements ReadableResource {
    private final SptTestCaseResourceKey key;
    private final String text;
    private final Instant modified;

    public SptTestCaseResource(SptTestCaseResourceKey key, String text) {
        this.key = key;
        this.text = text;
        this.modified = Instant.now();
    }

    @Override public void close() {
        // Nothing to close.
    }


    @Override public SptTestCaseResourceKey getKey() {
        return key;
    }

    @Override public boolean exists() {
        return true; // Always exists.
    }

    @Override public boolean isReadable() {
        return true; // Always readable.
    }

    @Override public Instant getLastModifiedTime() {
        return modified;
    }

    @Override public long getSize() {
        return text.length() * 2L; // UTF-16 is 2 bytes per character.
    }

    @Override
    public InputStream openRead() {
        return new ByteArrayInputStream(readBytes());
    }

    @Override
    public byte[] readBytes() {
        return text.getBytes(StandardCharsets.UTF_8); // Encode as UTF-8 bytes.
    }

    @Override
    public String readString(Charset fromCharset) {
        return text; // Ignore the character set, we do not need to decode from bytes.
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SptTestCaseResource that = (SptTestCaseResource)o;
        if(!key.equals(that.key)) return false;
        return text.equals(that.text);
    }

    @Override public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }

    @Override public String toString() {
        return key.toString();
    }
}
