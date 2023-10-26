package mb.spoofax.lwb.compiler;

import mb.cfg.CfgComponent;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.dynamix.DynamixComponent;
import mb.dynamix.task.DynamixConfig;
import mb.esv.EsvComponent;
import mb.esv.task.EsvConfig;
import mb.gpp.GppComponent;
import mb.gpp.GppResourcesComponent;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libspoofax2.LibSpoofax2ResourcesComponent;
import mb.libstatix.LibStatixComponent;
import mb.libstatix.LibStatixResourcesComponent;
import mb.llvm.LLVMComponent;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.Sdf3Component;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3_ext_dynamix.Sdf3ExtDynamixComponent;
import mb.sdf3_ext_statix.Sdf3ExtStatixComponent;
import mb.spoofax.core.Coordinate;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.Version;
import mb.spoofax.core.component.ComponentDependencyResolver;
import mb.spoofax.core.component.EmptyParticipant;
import mb.spoofax.core.component.SubcomponentRegistry;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.lwb.compiler.dynamix.SpoofaxDynamixConfig;
import mb.spoofax.lwb.compiler.dynamix.SpoofaxDynamixConfigureException;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvConfig;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvConfigureException;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Config;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3ConfigureException;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixConfig;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixConfigureException;
import mb.spoofax.lwb.compiler.stratego.SpoofaxStrategoConfigureException;
import mb.statix.StatixComponent;
import mb.statix.task.StatixConfig;
import mb.str.StrategoComponent;
import mb.str.config.StrategoAnalyzeConfig;
import mb.str.config.StrategoCompileConfig;
import mb.strategolib.StrategoLibComponent;
import mb.strategolib.StrategoLibResourcesComponent;
import mb.tim.TimComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SpoofaxLwbCompilerParticipant<L extends LoggerComponent, R extends ResourceServiceComponent, P extends PlatformComponent> extends EmptyParticipant<L, R, P> {
    private final SpoofaxLwbCompilerModule spoofaxLwbCompilerModule;
    private final SpoofaxLwbCompilerJavaModule spoofaxLwbCompilerJavaModule;

    private @Nullable SpoofaxLwbCompilerComponent component;

    public SpoofaxLwbCompilerParticipant(
        SpoofaxLwbCompilerModule spoofaxLwbCompilerModule,
        SpoofaxLwbCompilerJavaModule spoofaxLwbCompilerJavaModule
    ) {
        this.spoofaxLwbCompilerModule = spoofaxLwbCompilerModule;
        this.spoofaxLwbCompilerJavaModule = spoofaxLwbCompilerJavaModule;
    }


    @Override
    public Coordinate getCoordinate() {
        return new Coordinate("org.metaborg", "spoofax.lwb.compiler", new Version(0, 1, 0)); // TODO: get real version.
    }

    @Override public ListView<CoordinateRequirement> getDependencies() {
        final String groupId = "org.metaborg";
        return ListView.of(
            new CoordinateRequirement(groupId, "cfg"),
            new CoordinateRequirement(groupId, "sdf3"),
            new CoordinateRequirement(groupId, "stratego"),
            new CoordinateRequirement(groupId, "statix"),
            new CoordinateRequirement(groupId, "dynamix"),
            new CoordinateRequirement(groupId, "tim"),
            new CoordinateRequirement(groupId, "llvm"),

            new CoordinateRequirement(groupId, "sdf3_ext_statix"),
            new CoordinateRequirement(groupId, "sdf3_ext_dynamix"),

            new CoordinateRequirement(groupId, "strategolib"),
            new CoordinateRequirement(groupId, "gpp"),

            new CoordinateRequirement(groupId, "libspoofax2"),
            new CoordinateRequirement(groupId, "libstatix")
        );
    }

    @Override
    public @Nullable String getCompositionGroup() {
        return "mb.spoofax.lwb";
    }


    @Override
    public @Nullable TaskDefsProvider getTaskDefsProvider(
        L loggerComponent,
        R baseResourceServiceComponent,
        ResourceServiceComponent resourceServiceComponent,
        P platformComponent,
        SubcomponentRegistry subcomponentRegistry,
        ComponentDependencyResolver dependencyResolver
    ) {
        if(component != null) return component;

        // Get subcomponent dependencies.
        final CfgComponent cfgComponent = dependencyResolver.getOneSubcomponent(CfgComponent.class).unwrap();
        final Sdf3Component sdf3Component = dependencyResolver.getOneSubcomponent(Sdf3Component.class).unwrap();
        final StrategoComponent strategoComponent = dependencyResolver.getOneSubcomponent(StrategoComponent.class).unwrap();
        final EsvComponent esvComponent = dependencyResolver.getOneSubcomponent(EsvComponent.class).unwrap();
        final StatixComponent statixComponent = dependencyResolver.getOneSubcomponent(StatixComponent.class).unwrap();
        final DynamixComponent dynamixComponent = dependencyResolver.getOneSubcomponent(DynamixComponent.class).unwrap();
        final TimComponent timComponent = dependencyResolver.getOneSubcomponent(TimComponent.class).unwrap();
        final LLVMComponent llvmComponent = dependencyResolver.getOneSubcomponent(LLVMComponent.class).unwrap();

        final Sdf3ExtStatixComponent sdf3ExtStatixComponent = dependencyResolver.getOneSubcomponent(Sdf3ExtStatixComponent.class).unwrap();
        final Sdf3ExtDynamixComponent sdf3ExtDynamixComponent = dependencyResolver.getOneSubcomponent(Sdf3ExtDynamixComponent.class).unwrap();

        final StrategoLibComponent strategoLibComponent = dependencyResolver.getOneSubcomponent(StrategoLibComponent.class).unwrap();
        final StrategoLibResourcesComponent strategoLibResourcesComponent = dependencyResolver.getOneSubcomponent(StrategoLibResourcesComponent.class).unwrap();
        final GppComponent gppComponent = dependencyResolver.getOneSubcomponent(GppComponent.class).unwrap();
        final GppResourcesComponent gppResourcesComponent = dependencyResolver.getOneSubcomponent(GppResourcesComponent.class).unwrap();

        final LibSpoofax2Component libSpoofax2Component = dependencyResolver.getOneSubcomponent(LibSpoofax2Component.class).unwrap();
        final LibSpoofax2ResourcesComponent libSpoofax2ResourcesComponent = dependencyResolver.getOneSubcomponent(LibSpoofax2ResourcesComponent.class).unwrap();
        final LibStatixComponent libStatixComponent = dependencyResolver.getOneSubcomponent(LibStatixComponent.class).unwrap();
        final LibStatixResourcesComponent libStatixResourcesComponent = dependencyResolver.getOneSubcomponent(LibStatixResourcesComponent.class).unwrap();

        // Build component
        final SpoofaxLwbCompilerComponent component = DaggerSpoofaxLwbCompilerComponent.builder()
            .spoofaxLwbCompilerModule(spoofaxLwbCompilerModule)
            .spoofaxLwbCompilerJavaModule(spoofaxLwbCompilerJavaModule)

            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)

            .cfgComponent(cfgComponent)
            .sdf3Component(sdf3Component)
            .strategoComponent(strategoComponent)
            .esvComponent(esvComponent)
            .statixComponent(statixComponent)
            .timComponent(timComponent)
            .dynamixComponent(dynamixComponent)
            .lLVMComponent(llvmComponent)

            .sdf3ExtStatixComponent(sdf3ExtStatixComponent)
            .sdf3ExtDynamixComponent(sdf3ExtDynamixComponent)

            .strategoLibComponent(strategoLibComponent)
            .strategoLibResourcesComponent(strategoLibResourcesComponent)
            .gppComponent(gppComponent)
            .gppResourcesComponent(gppResourcesComponent)

            .libSpoofax2Component(libSpoofax2Component)
            .libSpoofax2ResourcesComponent(libSpoofax2ResourcesComponent)
            .libStatixComponent(libStatixComponent)
            .libStatixResourcesComponent(libStatixResourcesComponent)
            .build();

        // Set configuration functions so that the meta-languages get their configuration from CFG.
        sdf3Component.getSdf3SpecConfigFunctionWrapper().set(component.getSpoofaxSdf3Configure().createFunction().mapOutput(
            new StatelessSerializableFunction<Result<Option<SpoofaxSdf3Config>, SpoofaxSdf3ConfigureException>, Result<Option<Sdf3SpecConfig>, SpoofaxSdf3ConfigureException>>() {
                @Override
                public Result<Option<Sdf3SpecConfig>, SpoofaxSdf3ConfigureException> apply(Result<Option<SpoofaxSdf3Config>, SpoofaxSdf3ConfigureException> r) {
                    return r.map(o -> o.flatMap(SpoofaxSdf3Config::getMainSdf3SpecConfig));
                }
            }
        ));
        esvComponent.getEsvConfigFunctionWrapper().set(component.getSpoofaxEsvConfigure().createFunction().mapOutput(
            new StatelessSerializableFunction<Result<Option<SpoofaxEsvConfig>, SpoofaxEsvConfigureException>, Result<Option<EsvConfig>, SpoofaxEsvConfigureException>>() {
                @Override
                public Result<Option<EsvConfig>, SpoofaxEsvConfigureException> apply(Result<Option<SpoofaxEsvConfig>, SpoofaxEsvConfigureException> r) {
                    return r.map(o -> o.flatMap(SpoofaxEsvConfig::getEsvConfig));
                }
            }));
        strategoComponent.getStrategoAnalyzeConfigFunctionWrapper().set(component.getSpoofaxStrategoConfigure().createFunction().mapOutput(
            new StatelessSerializableFunction<Result<Option<StrategoCompileConfig>, SpoofaxStrategoConfigureException>, Result<Option<StrategoAnalyzeConfig>, SpoofaxStrategoConfigureException>>() {
                @Override
                public Result<Option<StrategoAnalyzeConfig>, SpoofaxStrategoConfigureException> apply(Result<Option<StrategoCompileConfig>, SpoofaxStrategoConfigureException> r) {
                    return r.map(o -> o.map(StrategoCompileConfig::toAnalyzeConfig));
                }
            }));
        statixComponent.getStatixConfigFunctionWrapper().set(component.getSpoofaxStatixConfigure().createFunction().mapOutput(
            new StatelessSerializableFunction<Result<Option<SpoofaxStatixConfig>, SpoofaxStatixConfigureException>, Result<Option<StatixConfig>, SpoofaxStatixConfigureException>>() {
                @Override
                public Result<Option<StatixConfig>, SpoofaxStatixConfigureException> apply(Result<Option<SpoofaxStatixConfig>, SpoofaxStatixConfigureException> r) {
                    return r.map(o -> o.flatMap(SpoofaxStatixConfig::getStatixConfig));
                }
            }));
        dynamixComponent.getDynamixConfigFunctionWrapper().set(component.getSpoofaxDynamixConfigure().createFunction().mapOutput(
            new StatelessSerializableFunction<Result<Option<SpoofaxDynamixConfig>, SpoofaxDynamixConfigureException>, Result<Option<DynamixConfig>, SpoofaxDynamixConfigureException>>() {
                @Override
                public Result<Option<DynamixConfig>, SpoofaxDynamixConfigureException> apply(Result<Option<SpoofaxDynamixConfig>, SpoofaxDynamixConfigureException> r) {
                    return r.map(o -> o.flatMap(SpoofaxDynamixConfig::getDynamixConfig));
                }
            }));

        // Register Spoofax3CompilerComponent as a subcomponent.
        subcomponentRegistry.register(SpoofaxLwbCompilerComponent.class, component);

        this.component = component;
        return component;
    }

    @Override public void close() {
        if(component != null) {
            component.close();
            component = null;
        }
    }
}
