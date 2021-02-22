package mb.spoofax.compiler.spoofax2.dagger;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.TaskDef;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.compiler.spoofax2.language.Spoofax2LanguageProjectCompiler;

import java.util.Set;

@Spoofax2CompilerScope
@Component(
    modules = {
        Spoofax2CompilerModule.class
    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class
    }
)
public interface Spoofax2CompilerComponent extends TaskDefsProvider {
    Spoofax2LanguageProjectCompiler getSpoofax2LanguageProjectCompiler();

    @Override @Spoofax2CompilerQualifier Set<TaskDef<?, ?>> getTaskDefs();
}
