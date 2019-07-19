package mb.spoofax.core.language.transform;

import java.io.Serializable;

public enum TransformExecutionType implements Serializable {
    OneShot,
    ContinuousOnResource,
    ContinuousOnEditor
}
