package mb.spoofax.compiler.spoofaxcore;

import dagger.Module;
import dagger.Provides;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.TaskDef;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;

import javax.inject.Singleton;
import java.util.Set;
import java.util.function.Supplier;

@Module
public class SpoofaxCompilerTestModule {
    private final Supplier<PieBuilder> builderSupplier;

    public SpoofaxCompilerTestModule(Supplier<PieBuilder> builderSupplier) {
        this.builderSupplier = builderSupplier;
    }

    @Provides @Singleton public ResourceService provideResourceService() {
        return new DefaultResourceService(new FSResourceRegistry());
    }

    @Provides @Singleton public Pie providePie(Set<TaskDef<?, ?>> taskDefs, ResourceService resourceService) {
        final PieBuilder builder = builderSupplier.get();
        builder.withTaskDefs(new MapTaskDefs(taskDefs));
        builder.withResourceService(resourceService);
        return builder.build();
    }
}
