package mb.spoofax.core.language.transform;

import mb.resource.ResourceKey;

public class ProjectSubject extends ResourceSubject {
    public ProjectSubject(ResourceKey resourceKey) {
        super(resourceKey);
    }

    public ResourceKey getProject() {
        return resourceKey;
    }

    @Override public void accept(TransformSubjectVisitor visitor) {
        visitor.project(resourceKey);
    }
}
