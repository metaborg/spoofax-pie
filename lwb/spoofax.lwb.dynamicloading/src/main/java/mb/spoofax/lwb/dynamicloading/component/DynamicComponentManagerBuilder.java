package mb.spoofax.lwb.dynamicloading.component;

import mb.common.util.ListView;
import mb.log.api.LoggerFactory;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.serde.JavaSerde;
import mb.pie.api.serde.Serde;
import mb.pie.dagger.RootPieModule;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.component.StaticComponentManager;
import mb.spoofax.core.platform.PlatformComponent;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class DynamicComponentManagerBuilder<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> {
    private final ArrayList<Consumer<RootPieModule>> pieModuleCustomizers = new ArrayList<>();
    private BiFunction<LoggerFactory, ClassLoader, Serde> serdeFactory = (loggerFactory, classLoader) -> new JavaSerde(classLoader);

    /**
     * Registers a {@link RootPieModule} customizer that is applied to dynamically loaded components. These customizers
     * are applied in the order that they are registered.
     *
     * @param customizer Customizer function to apply.
     * @return {@code this} for chaining.
     */
    public DynamicComponentManagerBuilder<L, R, P> registerPieModuleCustomizer(Consumer<RootPieModule> customizer) {
        pieModuleCustomizers.add(customizer);
        return this;
    }

    /**
     * Sets the {@link Serde serialization and deserialization} factory function to be used for dynamically loaded
     * components. The {@link ClassLoader class loader} passed as input to the function must be used for
     * deserialization, as dynamically loaded components use a special class loader.
     *
     * @param serdeFactory Factory function.
     * @return {@code this} for chaining.
     */
    public DynamicComponentManagerBuilder<L, R, P> withSerdeFactory(BiFunction<LoggerFactory, ClassLoader, Serde> serdeFactory) {
        this.serdeFactory = serdeFactory;
        return this;
    }

    /**
     * Builds the {@link DynamicComponentManagerImpl dynamic component manager}.
     *
     * @param staticComponentManager {@link StaticComponentManager Static component manager} to use as a base.
     * @return Dynamic component manager.
     */
    public DynamicComponentManagerImpl<L, R, P> build(StaticComponentManager<L, R, P> staticComponentManager) {
        return new DynamicComponentManagerImpl<>(staticComponentManager, ListView.of(pieModuleCustomizers), serdeFactory);
    }
}
