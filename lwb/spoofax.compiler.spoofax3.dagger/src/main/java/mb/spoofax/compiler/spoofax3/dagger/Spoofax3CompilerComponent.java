package mb.spoofax.compiler.spoofax3.dagger;

import dagger.Component;
import mb.esv.EsvComponent;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libspoofax2.LibSpoofax2ResourcesComponent;
import mb.libstatix.LibStatixComponent;
import mb.libstatix.LibStatixResourcesComponent;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.TaskDef;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.sdf3.Sdf3Component;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.statix.StatixComponent;
import mb.str.StrategoComponent;

import java.util.Set;

@Spoofax3CompilerScope
@Component(
    modules = {
        Spoofax3CompilerModule.class
    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class,
        Sdf3Component.class,
        StrategoComponent.class,
        EsvComponent.class,
        StatixComponent.class,
        LibSpoofax2Component.class,
        LibSpoofax2ResourcesComponent.class,
        LibStatixComponent.class,
        LibStatixResourcesComponent.class
    }
)
public interface Spoofax3CompilerComponent extends TaskDefsProvider {
    Spoofax3LanguageProjectCompiler getSpoofax3LanguageProjectCompiler();

    @Override @Spoofax3CompilerQualifier Set<TaskDef<?, ?>> getTaskDefs();
}
