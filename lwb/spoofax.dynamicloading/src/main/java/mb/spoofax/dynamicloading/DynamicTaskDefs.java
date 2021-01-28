package mb.spoofax.dynamicloading;

import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DynamicTaskDefs implements TaskDefs {
    private @Nullable TaskDefs taskDefs;

    public DynamicTaskDefs() {}

    public DynamicTaskDefs(TaskDefs taskDefs) {
        this.taskDefs = taskDefs;
    }

    public void setTaskDefs(TaskDefs taskDefs) {
        this.taskDefs = taskDefs;
    }

    @Override public @Nullable TaskDef<?, ?> getTaskDef(String id) {
        if(taskDefs == null) return null;
        return taskDefs.getTaskDef(id);
    }

    @Override public boolean exists(String id) {
        if(taskDefs == null) return false;
        return taskDefs.exists(id);
    }
}
