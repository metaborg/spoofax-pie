package mb.spoofax.core.language.transform;

import mb.common.util.EnumSetView;
import mb.pie.api.Task;
import mb.spoofax.core.language.transform.param.ParamDef;
import mb.spoofax.core.language.transform.param.RawArgs;

import java.io.Serializable;

public interface TransformDef<A extends Serializable> {
    String getId();

    String getDisplayName();


    EnumSetView<TransformExecutionType> getSupportedExecutionTypes();

    EnumSetView<TransformContextType> getSupportedContextTypes();


    ParamDef getParamDef();

    A fromRawArgs(RawArgs rawArgs);


    Task<TransformOutput> createTask(TransformInput<A> input);
}
