package mb.spoofax.lwb.compiler.dagger;

import mb.cfg.CfgParticipant;
import mb.esv.EsvParticipant;
import mb.gpp.GppParticipant;
import mb.libspoofax2.LibSpoofax2Participant;
import mb.libstatix.LibStatixParticipant;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.PieBuilder;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.Sdf3Participant;
import mb.sdf3_ext_statix.Sdf3ExtStatixParticipant;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.dagger.SpoofaxCompilerParticipant;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.component.ComponentGroup;
import mb.spoofax.core.component.ComponentManager;
import mb.spoofax.core.component.Participant;
import mb.spoofax.core.component.StaticComponentManagerBuilder;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.StatixParticipant;
import mb.str.StrategoParticipant;
import mb.strategolib.StrategoLibParticipant;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Facade for easily creating a {@link Spoofax3CompilerComponent}.
 */
public class Spoofax3Compiler implements AutoCloseable {
    public final ComponentManager componentManager;
    public final ResourceServiceComponent resourceServiceComponent;
    public final SpoofaxCompilerComponent spoofaxCompilerComponent;
    public final Spoofax3CompilerComponent spoofax3CompilerComponent;
    public final PieComponent pieComponent;

    private Spoofax3Compiler(
        ComponentManager componentManager,
        ResourceServiceComponent resourceServiceComponent,
        SpoofaxCompilerComponent spoofaxCompilerComponent,
        Spoofax3CompilerComponent spoofax3CompilerComponent,
        PieComponent pieComponent
    ) {
        this.componentManager = componentManager;
        this.resourceServiceComponent = resourceServiceComponent;
        this.spoofaxCompilerComponent = spoofaxCompilerComponent;
        this.spoofax3CompilerComponent = spoofax3CompilerComponent;
        this.pieComponent = pieComponent;
    }

    @Override public void close() {
        pieComponent.close();
        spoofax3CompilerComponent.close();
        spoofaxCompilerComponent.close();
        resourceServiceComponent.close();
        componentManager.close();
    }

    /**
     * Creates a {@link Spoofax3Compiler} from a {@link ComponentManager} which was instantiated with the correct {@link
     * Participant}s (see {@link #registerParticipants}). This will throw a {@link NoSuchElementException} when it is
     * not instantiated with the correct participants.
     */
    public static Spoofax3Compiler fromComponentManager(ComponentManager componentManager) {
        final ComponentGroup componentGroup = componentManager.getComponentGroup("mb.spoofax.lwb").unwrap();
        final ResourceServiceComponent resourceServiceComponent = componentGroup.getResourceServiceComponent();
        final SpoofaxCompilerComponent spoofaxCompilerComponent = componentManager.getOneSubcomponent(SpoofaxCompilerComponent.class).unwrap();
        final Spoofax3CompilerComponent spoofax3CompilerComponent = componentManager.getOneSubcomponent(Spoofax3CompilerComponent.class).unwrap();
        final PieComponent pieComponent = componentGroup.getPieComponent();
        return new Spoofax3Compiler(componentManager, resourceServiceComponent, spoofaxCompilerComponent, spoofax3CompilerComponent, pieComponent);
    }

    /**
     * Creates a {@link Spoofax3Compiler} from a {@link StaticComponentManagerBuilder} by first registering the correct
     * {@link Participant}s (see {@link #registerParticipants}), and then by building the {@link ComponentManager}.
     *
     * @param spoofax3CompilerJavaModule Java compiler module to pass to the builder for the {@link
     *                                   Spoofax3CompilerComponent}.
     */
    public static Spoofax3Compiler fromComponentBuilder(
        StaticComponentManagerBuilder<LoggerComponent, ResourceServiceComponent, PlatformComponent> builder,
        Spoofax3CompilerJavaModule spoofax3CompilerJavaModule
    ) {
        registerParticipants(builder, spoofax3CompilerJavaModule);
        final ComponentManager componentManager = builder.build();
        return fromComponentManager(componentManager);
    }

    /**
     * Creates a {@link Spoofax3Compiler} from a {@link StaticComponentManagerBuilder} by first registering the correct
     * {@link Participant}s (see {@link #registerParticipants}), and then by building the {@link ComponentManager}.
     */
    public static Spoofax3Compiler fromComponentBuilder(
        StaticComponentManagerBuilder<LoggerComponent, ResourceServiceComponent, PlatformComponent> builder
    ) {
        return fromComponentBuilder(builder, new Spoofax3CompilerJavaModule());
    }

    /**
     * Creates a {@link Spoofax3Compiler} by first creating a {@link StaticComponentManagerBuilder} from given base
     * components, then registering the correct {@link Participant}s (see {@link #registerParticipants}), and finally by
     * building the {@link ComponentManager}.
     */
    public static Spoofax3Compiler fromComponents(
        LoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent,
        Supplier<PieBuilder> pieBuilderSupplier
    ) {
        return fromComponentBuilder(new StaticComponentManagerBuilder<>(loggerComponent, resourceServiceComponent, platformComponent, pieBuilderSupplier));
    }

    /**
     * Registers the {@link Participant}s with given {@code builder} which are required to create a {@link
     * Spoofax3CompilerComponent}.
     *
     * @param spoofax3CompilerJavaModule Java compiler module to pass to the builder for the {@link
     *                                   Spoofax3CompilerComponent}.
     */
    public static <L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> void registerParticipants(
        StaticComponentManagerBuilder<L, R, P> builder,
        Spoofax3CompilerJavaModule spoofax3CompilerJavaModule
    ) {
        builder.registerParticipant(new CfgParticipant<>());
        builder.registerParticipant(new Sdf3Participant<>());
        builder.registerParticipant(new StrategoParticipant<>());
        builder.registerParticipant(new EsvParticipant<>());
        builder.registerParticipant(new StatixParticipant<>());

        builder.registerParticipant(new Sdf3ExtStatixParticipant<>());

        builder.registerParticipant(new StrategoLibParticipant<>());
        builder.registerParticipant(new GppParticipant<>());

        builder.registerParticipant(new LibSpoofax2Participant<>());
        builder.registerParticipant(new LibStatixParticipant<>());

        final TemplateCompiler templateCompiler = new TemplateCompiler(StandardCharsets.UTF_8);
        builder.registerParticipant(new SpoofaxCompilerParticipant<>(new SpoofaxCompilerModule(templateCompiler)));
        builder.registerParticipant(new Spoofax3CompilerParticipant<>(new Spoofax3CompilerModule(templateCompiler), spoofax3CompilerJavaModule));
    }

    /**
     * Registers the {@link Participant}s with given {@code builder} which are required to create a {@link
     * Spoofax3CompilerComponent}.
     */
    public static <L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> void registerParticipants(
        StaticComponentManagerBuilder<L, R, P> builder
    ) {
        registerParticipants(builder, new Spoofax3CompilerJavaModule());
    }
}
