package mb.spoofax.compiler.spoofax2.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.resource.ResourceService;
import mb.spoofax.compiler.spoofax2.language.Spoofax2ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2MultilangAnalyzerLanguageCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2ParserLanguageCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.spoofax2.language.Spoofax2StylerLanguageCompiler;

import java.util.HashSet;
import java.util.Set;

@Module
public class Spoofax2CompilerModule {
    private final ResourceService parentResourceService;
    private final Pie parentPie;

    public Spoofax2CompilerModule(ResourceService parentResourceService, Pie parentPie) {
        this.parentResourceService = parentResourceService;
        this.parentPie = parentPie;
    }


    @Provides @Spoofax2CompilerScope
    public ResourceService provideResourceService() {
        return parentResourceService;
    }

    @Provides @Spoofax2CompilerScope @ElementsIntoSet static Set<TaskDef<?, ?>> provideTaskDefsSet(
        Spoofax2LanguageProjectCompiler languageProjectCompiler,
        Spoofax2ParserLanguageCompiler parserLanguageCompiler,
        Spoofax2StylerLanguageCompiler stylerLanguageCompiler,
        Spoofax2ConstraintAnalyzerLanguageCompiler constraintAnalyzerLanguageCompiler,
        Spoofax2MultilangAnalyzerLanguageCompiler multilangAnalyzerLanguageCompiler,
        Spoofax2StrategoRuntimeLanguageCompiler strategoRuntimeLanguageCompiler
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(languageProjectCompiler);
        taskDefs.add(parserLanguageCompiler);
        taskDefs.add(stylerLanguageCompiler);
        taskDefs.add(constraintAnalyzerLanguageCompiler);
        taskDefs.add(multilangAnalyzerLanguageCompiler);
        taskDefs.add(strategoRuntimeLanguageCompiler);
        return taskDefs;
    }

    @Provides @Spoofax2CompilerScope
    public Pie providePie(ResourceService resourceService, Set<TaskDef<?, ?>> taskDefs) {
        return parentPie.createChildBuilder()
            .withTaskDefs(new MapTaskDefs(taskDefs))
            .withResourceService(resourceService)
            .build();
    }
}
