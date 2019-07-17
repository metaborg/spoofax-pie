package mb.spoofax.core.language.transform;

import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class ProjectSubject implements TransformSubject {
    private final ResourceKey project;


    public ProjectSubject(ResourceKey project) {
        this.project = project;
    }


    public ResourceKey getProject() {
        return project;
    }


    @Override public void accept(TransformSubjectVisitor visitor) {
        visitor.project(project);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final ProjectSubject other = (ProjectSubject) obj;
        return project.equals(other.project);
    }

    @Override public int hashCode() {
        return Objects.hash(project);
    }

    @Override public String toString() {
        return project.toString();
    }
}
