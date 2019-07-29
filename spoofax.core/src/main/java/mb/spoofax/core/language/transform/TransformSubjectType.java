package mb.spoofax.core.language.transform;

import java.io.Serializable;

public enum TransformSubjectType implements Serializable {
    Project,
    Directory,
    File,
    FileWithRegion,
    FileWithOffset,
    Readable,
    ReadableWithRegion,
    ReadableWithOffset,
    None
}
