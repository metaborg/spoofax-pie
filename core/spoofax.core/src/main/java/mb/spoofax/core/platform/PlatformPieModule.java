package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.log.api.LoggerFactory;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.TaskDef;
import mb.resource.ResourceService;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Module
public class PlatformPieModule {
    private final Supplier<PieBuilder> builderSupplier;

    public PlatformPieModule(Supplier<PieBuilder> builderSupplier) {
        this.builderSupplier = builderSupplier;
    }

    @Provides @Platform @Singleton @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefs() {
        return new HashSet<>();
    }

    @Provides @Platform @Singleton
    public Pie providePie(
        LoggerFactory loggerFactory,
        @Platform Set<TaskDef<?, ?>> taskDefs,
        @Platform ResourceService resourceService
    ) {
        final PieBuilder builder = builderSupplier.get();
        builder.withLoggerFactory(loggerFactory);
        builder.withTaskDefs(new MapTaskDefs(taskDefs));
        builder.withResourceService(resourceService);
        return builder.build();
    }
}
