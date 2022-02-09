package mb.spoofax.lwb.dynamicloading;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;

import java.util.HashSet;
import java.util.Set;

@Module
public abstract class DynamicLoadingPieModule {
    @Provides @DynamicLoadingQualifier @DynamicLoadingScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        DynamicLoad dynamicLoad
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(dynamicLoad);
        return taskDefs;
    }
}
