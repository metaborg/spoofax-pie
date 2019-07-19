package mb.spoofax.core.language.transform;

import mb.common.util.EnumSetView;
import mb.pie.api.Task;

public interface TransformDef {
    String getId();

    String getDisplayName();


    EnumSetView<TransformExecutionType> getSupportedExecutionTypes();

    EnumSetView<TransformSubjectType> getSupportedSubjectTypes();


    Task<TransformOutput> createTask(TransformInput input);
}
