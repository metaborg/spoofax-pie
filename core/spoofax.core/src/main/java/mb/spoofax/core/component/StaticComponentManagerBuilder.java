package mb.spoofax.core.component;

import mb.common.util.ListView;
import mb.common.util.MultiMap;
import mb.common.util.MultiMapView;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.PieBuilder;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.RootPieModule;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.ResourceServiceModule;
import mb.spoofax.core.platform.PlatformComponent;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StaticComponentManagerBuilder extends ComponentBuilder {
    private final ArrayList<Participant> participants = new ArrayList<>();
    private final ArrayList<Consumer<ResourceServiceModule>> resourceServiceModuleCustomizers = new ArrayList<>();
    private final MultiMap<String, Consumer<ResourceServiceModule>> groupedResourceServiceModuleCustomizers = MultiMap.withLinkedHash();
    private final ArrayList<Consumer<RootPieModule>> pieModuleCustomizers = new ArrayList<>();
    private final MultiMap<String, Consumer<RootPieModule>> groupedPieModuleCustomizers = MultiMap.withLinkedHash();


    public StaticComponentManagerBuilder(
        LoggerComponent loggerComponent,
        ResourceServiceComponent baseResourceServiceComponent,
        PlatformComponent platformComponent,
        Supplier<PieBuilder> pieBuilderSupplier
    ) {
        super(loggerComponent, baseResourceServiceComponent, platformComponent, pieBuilderSupplier);
    }


    /**
     * Registers a {@link Participant participant}. Participants that belong to the same group are instantiated
     * together:
     * <ul>
     * <li>
     * Their {@link Participant#getResourceRegistriesProvider resource registry providers} are all
     * {@link ResourceServiceModule#addRegistries added} together to form a single
     * {@link ResourceServiceComponent resource service component}. This allows the participants to access each other's
     * resources at runtime.
     * </li>
     * <li>
     * Their {@link Participant#getTaskDefsProvider task definition providers} are all
     * {@link RootPieModule#addTaskDefsFrom added} together to form a single {@link PieComponent PIE component}. This
     * allows the participants to participate in PIE builds together at runtime.
     * </li>
     * </ul>
     *
     * Participants without a group are instantiated in a standalone way.
     *
     * @param participant {@link Participant Participant} to register.
     */
    public void registerParticipant(Participant participant) {
        participants.add(participant);
    }


    /**
     * Registers a {@link ResourceServiceModule} customizer that is applied to independent and grouped participants.
     * These customizers are applied in the order that they are registered.
     *
     * @param customizer Customizer function to apply.
     */
    public void registerResourceServiceModuleCustomizer(Consumer<ResourceServiceModule> customizer) {
        resourceServiceModuleCustomizers.add(customizer);
    }

    /**
     * Registers a {@link ResourceServiceModule} customizer that is applied only to given {@code group}. These
     * customizers are applied in the order that they are registered, after first applying all ungrouped customizers.
     *
     * @param customizer Customizer function to apply.
     */
    public void registerResourceServiceModuleCustomizer(Consumer<ResourceServiceModule> customizer, String group) {
        groupedResourceServiceModuleCustomizers.put(group, customizer);
    }

    /**
     * Registers a {@link RootPieModule} customizer that is applied to independent and grouped participants. These
     * customizers are applied in the order that they are registered.
     *
     * @param customizer Customizer function to apply.
     */
    public void registerPieModuleCustomizer(Consumer<RootPieModule> customizer) {
        pieModuleCustomizers.add(customizer);
    }

    /**
     * Registers a {@link RootPieModule} customizer that is applied only to given {@code group}. These customizers are
     * applied in the order that they are registered, after first applying all ungrouped customizers.
     *
     * @param customizer Customizer function to apply.
     */
    public void registerPieModuleCustomizer(Consumer<RootPieModule> customizer, String group) {
        groupedPieModuleCustomizers.put(group, customizer);
    }


    /**
     * Composes all participants and builds a {@link ComponentManager language manager}, providing access to the
     * composed participants.
     *
     * @return {@link ComponentManager Language manager} providing access to the composed participants.
     */
    public StaticComponentManager build() {
        final ListView<Consumer<ResourceServiceModule>> resourceServiceModuleCustomizers = ListView.copyOf(this.resourceServiceModuleCustomizers);
        final MultiMapView<String, Consumer<ResourceServiceModule>> groupedResourceServiceModuleCustomizers = MultiMapView.copyOf(this.groupedResourceServiceModuleCustomizers);
        final ListView<Consumer<RootPieModule>> pieModuleCustomizers = ListView.copyOf(this.pieModuleCustomizers);
        final MultiMapView<String, Consumer<RootPieModule>> groupedPieModuleCustomizers = MultiMapView.copyOf(this.groupedPieModuleCustomizers);

        final BuildResult result = super.build(
            participants,
            resourceServiceModuleCustomizers,
            groupedResourceServiceModuleCustomizers,
            pieModuleCustomizers,
            groupedPieModuleCustomizers,
            null
        );

        return new StaticComponentManager(
            loggerComponent,
            platformComponent,

            result.standaloneComponents,
            result.groupedComponents,

            baseResourceServiceComponent,
            pieBuilderSupplier,
            result.globalResourceRegistryProviders,
            result.globalTaskDefsProviders,
            resourceServiceModuleCustomizers,
            groupedResourceServiceModuleCustomizers,
            pieModuleCustomizers,
            groupedPieModuleCustomizers
        );
    }
}
