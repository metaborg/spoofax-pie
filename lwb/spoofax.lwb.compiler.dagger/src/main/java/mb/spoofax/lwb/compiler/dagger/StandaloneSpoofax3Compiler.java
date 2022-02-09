package mb.spoofax.lwb.compiler.dagger;

import mb.cfg.CfgComponent;
import mb.cfg.CfgParticipant;
import mb.esv.EsvComponent;
import mb.esv.EsvParticipant;
import mb.gpp.GppComponent;
import mb.gpp.GppParticipant;
import mb.gpp.GppResourcesComponent;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libspoofax2.LibSpoofax2Participant;
import mb.libspoofax2.LibSpoofax2ResourcesComponent;
import mb.libstatix.LibStatixComponent;
import mb.libstatix.LibStatixParticipant;
import mb.libstatix.LibStatixResourcesComponent;
import mb.log.dagger.LoggerComponent;
import mb.pie.dagger.PieComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.Sdf3Component;
import mb.sdf3.Sdf3Participant;
import mb.sdf3_ext_statix.Sdf3ExtStatixComponent;
import mb.sdf3_ext_statix.Sdf3ExtStatixParticipant;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.dagger.SpoofaxCompilerParticipant;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.component.ComponentGroup;
import mb.spoofax.core.component.ComponentManager;
import mb.spoofax.core.component.StaticComponentManagerBuilder;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.StatixComponent;
import mb.statix.StatixParticipant;
import mb.str.StrategoComponent;
import mb.strategolib.StrategoLibComponent;
import mb.strategolib.StrategoLibParticipant;
import mb.strategolib.StrategoLibResourcesComponent;

import java.nio.charset.StandardCharsets;

public class StandaloneSpoofax3Compiler implements AutoCloseable {
    public final ComponentManager componentManager;
    public final Spoofax3Compiler compiler;
    public final PieComponent pieComponent;

    public StandaloneSpoofax3Compiler(
        StaticComponentManagerBuilder<LoggerComponent, ResourceServiceComponent, PlatformComponent> builder,
        Spoofax3CompilerJavaModule spoofax3CompilerJavaModule
    ) {
        builder.registerParticipant(new CfgParticipant<>());
        builder.registerParticipant(new Sdf3Participant<>());
        builder.registerParticipant(new StrategoLibParticipant<>());
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
        this.compiler = new Spoofax3Compiler(
            componentManager.getLoggerComponent(),
            componentGroup.getResourceServiceComponent(),
            componentManager.getPlatformComponent(),

            componentManager.getOneSubcomponent(CfgComponent.class).unwrap(),
            componentManager.getOneSubcomponent(Sdf3Component.class).unwrap(),
            componentManager.getOneSubcomponent(StrategoComponent.class).unwrap(),
            componentManager.getOneSubcomponent(EsvComponent.class).unwrap(),
            componentManager.getOneSubcomponent(StatixComponent.class).unwrap(),

            componentManager.getOneSubcomponent(Sdf3ExtStatixComponent.class).unwrap(),

            componentManager.getOneSubcomponent(StrategoLibComponent.class).unwrap(),
            componentManager.getOneSubcomponent(StrategoLibResourcesComponent.class).unwrap(),
            componentManager.getOneSubcomponent(GppComponent.class).unwrap(),
            componentManager.getOneSubcomponent(GppResourcesComponent.class).unwrap(),

            componentManager.getOneSubcomponent(LibSpoofax2Component.class).unwrap(),
            componentManager.getOneSubcomponent(LibSpoofax2ResourcesComponent.class).unwrap(),
            componentManager.getOneSubcomponent(LibStatixComponent.class).unwrap(),
            componentManager.getOneSubcomponent(LibStatixResourcesComponent.class).unwrap(),

            componentManager.getOneSubcomponent(SpoofaxCompilerComponent.class).unwrap(),
            componentManager.getOneSubcomponent(Spoofax3CompilerComponent.class).unwrap()
        );
        this.pieComponent = componentGroup.getPieComponent();
    }

    public StandaloneSpoofax3Compiler(
        StaticComponentManagerBuilder<LoggerComponent, ResourceServiceComponent, PlatformComponent> builder
    ) {
        this(builder, new Spoofax3CompilerJavaModule());
    }

    @Override public void close() {
        pieComponent.close();
        compiler.close();
        componentManager.close();
    }
}
