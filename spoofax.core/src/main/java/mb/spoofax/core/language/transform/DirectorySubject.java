package mb.spoofax.core.language.transform;

import mb.resource.ResourceKey;

public class DirectorySubject extends ResourceSubject {
    public DirectorySubject(ResourceKey resourceKey) {
        super(resourceKey);
    }

    public ResourceKey getDirectory() {
        return resourceKey;
    }

    @Override public void accept(TransformSubjectVisitor visitor) {
        visitor.directory(resourceKey);
    }
}
