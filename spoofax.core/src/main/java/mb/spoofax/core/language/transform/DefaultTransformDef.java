package mb.spoofax.core.language.transform;

import mb.pie.api.Task;
import mb.pie.api.TaskDef;

import java.util.EnumSet;

public class DefaultTransformDef implements TransformDef {
    private final String id;
    private final String displayName;
    private final EnumSet<TransformExecutionType> supportedExecutionTypes;
    private final EnumSet<TransformSubjectType> supportedSubjectTypes;
    private final TaskDef<TransformInput, TransformOutput> taskDef;


    public DefaultTransformDef(
        String id,
        String displayName,
        EnumSet<TransformExecutionType> supportedExecutionTypes,
        EnumSet<TransformSubjectType> supportedSubjectTypes,
        TaskDef<TransformInput, TransformOutput> taskDef
    ) {
        this.id = id;
        this.displayName = displayName;
        this.supportedExecutionTypes = supportedExecutionTypes;
        this.supportedSubjectTypes = supportedSubjectTypes;
        this.taskDef = taskDef;
    }


    @Override public String getId() {
        return id;
    }

    @Override public String getDisplayName() {
        return displayName;
    }

    @Override public EnumSet<TransformExecutionType> getSupportedExecutionTypes() {
        return supportedExecutionTypes;
    }

    @Override public EnumSet<TransformSubjectType> getSupportedSubjectTypes() {
        return supportedSubjectTypes;
    }

    @Override public Task<TransformOutput> createTask(TransformInput input) {
        return taskDef.createTask(input);
    }
}
