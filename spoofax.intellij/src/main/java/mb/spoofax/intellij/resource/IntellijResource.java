package mb.spoofax.intellij.resource;

import com.intellij.openapi.vfs.VirtualFile;
import mb.resource.ReadableResource;
import mb.resource.Resource;
import mb.resource.ResourceKey;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

public class IntellijResource implements Resource, ReadableResource {
    private final VirtualFile file;


    public IntellijResource(VirtualFile file) {
        this.file = file;
    }


    @Override public boolean exists() {
        return file.exists();
    }

    @Override public boolean isReadable() {
        return true;
    }

    @Override public Instant getLastModifiedTime() {
        return Instant.ofEpochMilli(file.getTimeStamp());
    }

    @Override public long getSize() {
        return file.getLength();
    }

    @Override public InputStream newInputStream() throws IOException {
        return file.getInputStream();
    }


    @Override public ResourceKey getKey() {
        return new IntellijResourceKey(file.getUrl());
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final IntellijResource that = (IntellijResource) o;
        return file.equals(that.file);
    }

    @Override public int hashCode() {
        return file.hashCode();
    }

    @Override public String toString() {
        return file.getPresentableUrl();
    }
}
