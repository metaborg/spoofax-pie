package mb.spoofax.core.pie;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;

import java.util.HashSet;
import java.util.Set;

@Module
public class SpoofaxTaskDefsModule {
    private final Set<TaskDef<?, ?>> taskDefs;

    public SpoofaxTaskDefsModule(Set<TaskDef<?, ?>> taskDefs) {
        this.taskDefs = taskDefs;
    }

    @SafeVarargs public SpoofaxTaskDefsModule(Set<TaskDef<?, ?>>... taskDefsArray) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        for(Set<TaskDef<?, ?>> taskDefsSet : taskDefsArray) {
            taskDefs.addAll(taskDefsSet);
        }
        this.taskDefs = taskDefs;
    }

    @Provides @ElementsIntoSet Set<TaskDef<?, ?>> provideTaskDefs() {
        return taskDefs;
    }
}
