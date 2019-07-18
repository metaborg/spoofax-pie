package mb.spoofax.core.language.transform;

import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class DirectorySubject implements TransformSubject {
    private final ResourcePath directory;


    public DirectorySubject(ResourcePath directory) {
        this.directory = directory;
    }


    public ResourcePath getDirectory() {
        return directory;
    }


    @Override public void accept(TransformSubjectVisitor visitor) {
        visitor.directory(directory);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final DirectorySubject other = (DirectorySubject) obj;
        return directory.equals(other.directory);
    }

    @Override public int hashCode() {
        return Objects.hash(directory);
    }

    @Override public String toString() {
        return directory.toString();
    }
}
