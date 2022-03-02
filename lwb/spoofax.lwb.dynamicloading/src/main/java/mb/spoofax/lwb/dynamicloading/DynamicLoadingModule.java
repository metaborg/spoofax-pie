package mb.spoofax.lwb.dynamicloading;

import dagger.Module;
import dagger.Provides;
import mb.common.util.ListView;
import mb.log.api.LoggerFactory;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.PieBuilder;
import mb.pie.api.serde.JavaSerde;
import mb.pie.api.serde.Serde;
import mb.pie.dagger.RootPieModule;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.component.StaticComponentManager;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManagerImpl;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Module
public class DynamicLoadingModule {
    private final Supplier<PieBuilder> pieBuilderSupplier;
    private final ArrayList<Consumer<RootPieModule>> pieModuleCustomizers = new ArrayList<>();
    private final BiFunction<LoggerFactory, ClassLoader, Serde> serdeFactory;
    private @Nullable StaticComponentManager baseComponentManager = null;

    public DynamicLoadingModule(
        Supplier<PieBuilder> pieBuilderSupplier,
        BiFunction<LoggerFactory, ClassLoader, Serde> serdeFactory
    ) {
        this.pieBuilderSupplier = pieBuilderSupplier;
        this.serdeFactory = serdeFactory;
    }

    public DynamicLoadingModule(Supplier<PieBuilder> pieBuilderSupplier) {
        this(pieBuilderSupplier, (loggerFactory, classLoader) -> new JavaSerde(classLoader));
    }


    public DynamicLoadingModule addPieModuleCustomizers(Consumer<RootPieModule> customizer) {
        this.pieModuleCustomizers.add(customizer);
        return this;
    }

    public DynamicLoadingModule withBaseComponentManager(StaticComponentManager baseComponentManager) {
        this.baseComponentManager = baseComponentManager;
        return this;
    }


    @Provides @DynamicLoadingScope
    DynamicComponentManager provideDynamicComponentManager(
        DynamicLoadGetBaseComponentManager dynamicLoadGetBaseComponentManager,
        LoggerComponent loggerComponent,
        ResourceServiceComponent baseResourceServiceComponent,
        PlatformComponent platformComponent
    ) {
        if(baseComponentManager != null) {
            dynamicLoadGetBaseComponentManager.set(baseComponentManager);
        }
        return new DynamicComponentManagerImpl<>(
            loggerComponent,
            baseResourceServiceComponent,
            platformComponent,
            pieBuilderSupplier,
            ListView.of(pieModuleCustomizers),
            serdeFactory
        );
    }
}
