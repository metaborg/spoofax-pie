package mb.spoofax.core.language.transform;

import mb.resource.ResourceKey;

public class FileSubject extends ResourceSubject {
    public FileSubject(ResourceKey resourceKey) {
        super(resourceKey);
    }

    public ResourceKey getFile() {
        return resourceKey;
    }

    @Override public void accept(TransformSubjectVisitor visitor) {
        visitor.file(resourceKey);
    }
}
