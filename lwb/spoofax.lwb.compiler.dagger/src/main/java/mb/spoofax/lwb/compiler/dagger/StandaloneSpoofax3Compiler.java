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
import mb.spoofax.core.component.StaticComponentBuilder;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.StatixParticipant;
import mb.str.StrategoParticipant;
import mb.strategolib.StrategoLibParticipant;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class StandaloneSpoofax3Compiler implements AutoCloseable {
    public final ComponentManager componentManager;
    public final Spoofax3CompilerComponent spoofax3CompilerComponent;
    public final SpoofaxCompilerComponent spoofaxCompilerComponent;
    public final PieComponent pieComponent;

    public StandaloneSpoofax3Compiler(
        StaticComponentBuilder<LoggerComponent, ResourceServiceComponent, PlatformComponent> builder,
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
        this.componentManager = builder.build();

        final ComponentGroup componentGroup = componentManager.getComponentGroup("mb.spoofax.lwb").unwrap();
        this.spoofax3CompilerComponent = componentManager.getOneSubcomponent(Spoofax3CompilerComponent.class).unwrap();
        this.spoofaxCompilerComponent = componentManager.getOneSubcomponent(SpoofaxCompilerComponent.class).unwrap();
        this.pieComponent = componentGroup.getPieComponent();
    }

    public StandaloneSpoofax3Compiler(StaticComponentBuilder<LoggerComponent, ResourceServiceComponent, PlatformComponent> builder) {
        this(builder, new Spoofax3CompilerJavaModule());
    }

    public StandaloneSpoofax3Compiler(
        LoggerComponent loggerComponent,
        ResourceServiceComponent resourceServiceComponent,
        PlatformComponent platformComponent,
        Supplier<PieBuilder> pieBuilderSupplier
    ) {
        this(new StaticComponentBuilder<>(loggerComponent, resourceServiceComponent, platformComponent, pieBuilderSupplier), new Spoofax3CompilerJavaModule());
    }

    @Override public void close() {
        pieComponent.close();
        spoofaxCompilerComponent.close();
        spoofax3CompilerComponent.close();
        componentManager.close();
    }
}
