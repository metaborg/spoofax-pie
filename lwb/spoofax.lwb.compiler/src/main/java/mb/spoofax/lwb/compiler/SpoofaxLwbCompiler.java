package mb.spoofax.lwb.compiler;

import mb.cfg.CfgParticipant;
import mb.dynamix.DynamixParticipant;
import mb.esv.EsvParticipant;
import mb.gpp.GppParticipant;
import mb.libspoofax2.LibSpoofax2Participant;
import mb.libstatix.LibStatixParticipant;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.PieBuilder;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.Sdf3Participant;
import mb.sdf3_ext_dynamix.Sdf3ExtDynamixParticipant;
import mb.sdf3_ext_statix.Sdf3ExtStatixParticipant;
import mb.spoofax.compiler.SpoofaxCompilerComponent;
import mb.spoofax.compiler.SpoofaxCompilerModule;
import mb.spoofax.compiler.SpoofaxCompilerParticipant;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.component.ComponentGroup;
import mb.spoofax.core.component.ComponentManager;
import mb.spoofax.core.component.Participant;
import mb.spoofax.core.component.StaticComponentManagerBuilder;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.StatixParticipant;
import mb.str.StrategoParticipant;
import mb.strategolib.StrategoLibParticipant;
import mb.tim.TimParticipant;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Facade for easily creating a {@link SpoofaxLwbCompilerComponent}.
 */
public class SpoofaxLwbCompiler implements AutoCloseable {
    public final ComponentManager componentManager;
    public final ResourceServiceComponent resourceServiceComponent;
    public final SpoofaxCompilerComponent spoofaxCompilerComponent;
    public final SpoofaxLwbCompilerComponent spoofaxLwbCompilerComponent;
    public final PieComponent pieComponent;

    private SpoofaxLwbCompiler(
        ComponentManager componentManager,
        ResourceServiceComponent resourceServiceComponent,
        SpoofaxCompilerComponent spoofaxCompilerComponent,
        SpoofaxLwbCompilerComponent spoofaxLwbCompilerComponent,
        PieComponent pieComponent
    ) {
        this.componentManager = componentManager;
        this.resourceServiceComponent = resourceServiceComponent;
        this.spoofaxCompilerComponent = spoofaxCompilerComponent;
        this.spoofaxLwbCompilerComponent = spoofaxLwbCompilerComponent;
        this.pieComponent = pieComponent;
    }

    @Override public void close() {
        pieComponent.close();
        spoofaxLwbCompilerComponent.close();
        spoofaxCompilerComponent.close();
        resourceServiceComponent.close();
        componentManager.close();
    }

    /**
     * Creates a {@link SpoofaxLwbCompiler} from a {@link ComponentManager} which was instantiated with the correct
     * {@link Participant}s (see {@link #registerParticipants}). This will throw a {@link NoSuchElementException} when
     * it is not instantiated with the correct participants.
     */
    public static SpoofaxLwbCompiler fromComponentManager(ComponentManager componentManager) {
        final ComponentGroup componentGroup = componentManager.getComponentGroup("mb.spoofax.lwb").unwrap();
        final ResourceServiceComponent resourceServiceComponent = componentGroup.getResourceServiceComponent();
        final SpoofaxCompilerComponent spoofaxCompilerComponent = componentManager.getOneSubcomponent(SpoofaxCompilerComponent.class).unwrap();
        final SpoofaxLwbCompilerComponent spoofaxLwbCompilerComponent = componentManager.getOneSubcomponent(SpoofaxLwbCompilerComponent.class).unwrap();
        spoofaxLwbCompilerComponent.getSpoofaxLwbCompilerComponentManagerWrapper().set(componentManager);
        final PieComponent pieComponent = componentGroup.getPieComponent();
        return new SpoofaxLwbCompiler(componentManager, resourceServiceComponent, spoofaxCompilerComponent, spoofaxLwbCompilerComponent, pieComponent);
    }

    /**
     * Creates a {@link SpoofaxLwbCompiler} from a {@link StaticComponentManagerBuilder} by first registering the
     * correct {@link Participant}s (see {@link #registerParticipants}), and then by building the {@link
     * ComponentManager}.
     *
     * @param spoofaxLwbCompilerJavaModule Java compiler module to pass to the builder for the {@link
     *                                     SpoofaxLwbCompilerComponent}.
     */
    public static SpoofaxLwbCompiler fromComponentBuilder(
        StaticComponentManagerBuilder<LoggerComponent, ResourceServiceComponent, PlatformComponent> builder,
        SpoofaxLwbCompilerJavaModule spoofaxLwbCompilerJavaModule
    ) {
        registerParticipants(builder, spoofaxLwbCompilerJavaModule);
        final ComponentManager componentManager = builder.build();
        return fromComponentManager(componentManager);
    }

    /**
     * Creates a {@link SpoofaxLwbCompiler} from a {@link StaticComponentManagerBuilder} by first registering the
     * correct {@link Participant}s (see {@link #registerParticipants}), and then by building the {@link
     * ComponentManager}.
     */
    public static SpoofaxLwbCompiler fromComponentBuilder(
        StaticComponentManagerBuilder<LoggerComponent, ResourceServiceComponent, PlatformComponent> builder
    ) {
        return fromComponentBuilder(builder, new SpoofaxLwbCompilerJavaModule());
    }

    /**
     * Creates a {@link SpoofaxLwbCompiler} by first creating a {@link StaticComponentManagerBuilder} from given base
     * components, then registering the correct {@link Participant}s (see {@link #registerParticipants}), and finally by
     * building the {@link ComponentManager}.
     */
    public static SpoofaxLwbCompiler fromComponents(
        LoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent,
        Supplier<PieBuilder> pieBuilderSupplier
    ) {
        return fromComponentBuilder(new StaticComponentManagerBuilder<>(loggerComponent, resourceServiceComponent, platformComponent, pieBuilderSupplier));
    }

    /**
     * Registers the {@link Participant}s with given {@code builder} which are required to create a {@link
     * SpoofaxLwbCompilerComponent}.
     *
     * @param spoofaxLwbCompilerJavaModule Java compiler module to pass to the builder for the {@link
     *                                     SpoofaxLwbCompilerComponent}.
     */
    public static <L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> void registerParticipants(
        StaticComponentManagerBuilder<L, R, P> builder,
        SpoofaxLwbCompilerJavaModule spoofaxLwbCompilerJavaModule
    ) {
        builder.registerParticipant(new CfgParticipant<>());
        builder.registerParticipant(new Sdf3Participant<>());
        builder.registerParticipant(new StrategoParticipant<>());
        builder.registerParticipant(new EsvParticipant<>());
        builder.registerParticipant(new StatixParticipant<>());
        builder.registerParticipant(new TimParticipant<>());
        builder.registerParticipant(new DynamixParticipant<>());

        builder.registerParticipant(new Sdf3ExtStatixParticipant<>());
        builder.registerParticipant(new Sdf3ExtDynamixParticipant<>());

        builder.registerParticipant(new StrategoLibParticipant<>());
        builder.registerParticipant(new GppParticipant<>());

        builder.registerParticipant(new LibSpoofax2Participant<>());
        builder.registerParticipant(new LibStatixParticipant<>());

        final TemplateCompiler templateCompiler = new TemplateCompiler(StandardCharsets.UTF_8);
        builder.registerParticipant(new SpoofaxCompilerParticipant<>(new SpoofaxCompilerModule(templateCompiler)));
        builder.registerParticipant(new SpoofaxLwbCompilerParticipant<>(new SpoofaxLwbCompilerModule(templateCompiler), spoofaxLwbCompilerJavaModule));
    }

    /**
     * Registers the {@link Participant}s with given {@code builder} which are required to create a {@link
     * SpoofaxLwbCompilerComponent}.
     */
    public static <L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> void registerParticipants(
        StaticComponentManagerBuilder<L, R, P> builder
    ) {
        registerParticipants(builder, new SpoofaxLwbCompilerJavaModule());
    }
}
