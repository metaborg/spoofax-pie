package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.log.api.LoggerFactory;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.TaskDef;
import mb.pie.api.Tracer;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Module
public class PlatformPieModule {
    private final Supplier<PieBuilder> builderSupplier;
    private final PieBuilder.@Nullable StoreFactory storeFactory;
    private final @Nullable Function<LoggerFactory, Tracer> tracerFactory;

    public PlatformPieModule(Supplier<PieBuilder> builderSupplier) {
        this(builderSupplier, null, null);
    }

    public PlatformPieModule(Supplier<PieBuilder> builderSupplier, PieBuilder.@Nullable StoreFactory storeFactory) {
        this(builderSupplier, storeFactory, null);
    }

    public PlatformPieModule(
        Supplier<PieBuilder> builderSupplier,
        PieBuilder.@Nullable StoreFactory storeFactory,
        @Nullable Function<LoggerFactory, Tracer> tracerFactory
    ) {
        this.builderSupplier = builderSupplier;
        this.storeFactory = storeFactory;
        this.tracerFactory = tracerFactory;
    }

    @Provides @Platform @PlatformScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefs() {
        return new HashSet<>();
    }

    @Provides @Platform /* Unscoped: create a fresh PieBuilder every time. */
    public PieBuilder providePieBuilder(
        LoggerFactory loggerFactory,
        @Platform Set<TaskDef<?, ?>> taskDefs,
        ResourceService resourceService
    ) {
        final PieBuilder builder = builderSupplier.get();
        builder.withTaskDefs(new MapTaskDefs(taskDefs));
        builder.withResourceService(resourceService);
        builder.withLoggerFactory(loggerFactory);
        if(storeFactory != null) {
            builder.withStoreFactory(storeFactory);
        }
        if(tracerFactory != null) {
            builder.withTracerFactory(tracerFactory);
        }
        return builder;
    }
}
