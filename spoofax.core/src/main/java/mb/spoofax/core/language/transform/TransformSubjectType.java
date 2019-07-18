package mb.spoofax.core.language.transform;

import java.io.Serializable;

public enum TransformSubjectType implements Serializable {
    None,
    Project,
    Directory,
    File,
    FileRegion
}
