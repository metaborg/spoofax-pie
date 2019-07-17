package mb.spoofax.core.language.transform;

public class TransformRequest {
    public final TransformDef transformDef;
    public final TransformExecutionType executionType;


    public TransformRequest(TransformDef transformDef, TransformExecutionType executionType) {
        this.transformDef = transformDef;
        this.executionType = executionType;
    }
}
