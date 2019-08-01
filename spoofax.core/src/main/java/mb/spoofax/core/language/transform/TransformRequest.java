package mb.spoofax.core.language.transform;

import java.io.Serializable;

public class TransformRequest<A extends Serializable> {
    public final TransformDef<A> transformDef;
    public final TransformExecutionType executionType;


    public TransformRequest(TransformDef<A> transformDef, TransformExecutionType executionType) {
        this.transformDef = transformDef;
        this.executionType = executionType;
    }
}
