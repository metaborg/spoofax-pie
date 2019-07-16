package mb.spoofax.core.language.transform;

import mb.pie.api.Task;

import java.util.EnumSet;

public interface TransformDef {
    String getId();

    String getDisplayName();


    EnumSet<TransformExecutionType> getSupportedExecutionTypes();

    EnumSet<TransformSubjectType> getSupportedSubjectTypes();


    Task<TransformOutput> createTask(TransformInput input);
}
