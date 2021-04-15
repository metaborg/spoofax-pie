package mb.spoofax.lwb.dynamicloading;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.PieBuilder;
import mb.pie.api.TaskDef;
import mb.pie.dagger.RootPieModule;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Module
public class DynamicLoadingPieModule {
    private final Supplier<RootPieModule> rootPieModuleSupplier;

    public DynamicLoadingPieModule(Supplier<RootPieModule> rootPieModuleSupplier) {
        this.rootPieModuleSupplier = rootPieModuleSupplier;
    }

    @Provides @DynamicLoadingQualifier @DynamicLoadingScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        DynamicLoad dynamicLoad
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(dynamicLoad);
        return taskDefs;
    }

    @Provides /* unscoped: new instance every call */
    RootPieModule provideRootPieModule() {
        return rootPieModuleSupplier.get();
    }
}
