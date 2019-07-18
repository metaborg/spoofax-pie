package mb.spoofax.core.language.transform;

import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class FileSubject implements TransformSubject {
    protected final ResourcePath file;


    public FileSubject(ResourcePath file) {
        this.file = file;
    }


    public ResourcePath getFile() {
        return file;
    }


    @Override public void accept(TransformSubjectVisitor visitor) {
        visitor.file(file);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final FileSubject other = (FileSubject) obj;
        return file.equals(other.file);
    }

    @Override public int hashCode() {
        return Objects.hash(file);
    }

    @Override public String toString() {
        return file.toString();
    }
}
