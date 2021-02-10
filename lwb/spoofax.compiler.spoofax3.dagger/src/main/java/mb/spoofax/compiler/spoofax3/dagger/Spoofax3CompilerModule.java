package mb.spoofax.compiler.spoofax3.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.esv.EsvQualifier;
import mb.libspoofax2.LibSpoofax2Qualifier;
import mb.libstatix.LibStatixQualifier;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.TaskDef;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.ResourceService;
import mb.sdf3.Sdf3Qualifier;
import mb.spoofax.compiler.spoofax3.language.Spoofax3ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3ParserLanguageCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3StylerLanguageCompiler;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.platform.Platform;
import mb.statix.StatixQualifier;
import mb.str.StrategoQualifier;

import java.util.HashSet;
import java.util.Set;

@Module public class Spoofax3CompilerModule {
    private final TemplateCompiler templateCompiler;

    public Spoofax3CompilerModule(TemplateCompiler templateCompiler) {
        this.templateCompiler = templateCompiler;
    }

    @Provides
    @Spoofax3CompilerScope
    public TemplateCompiler provideTemplateCompiler() {
        return templateCompiler;
    }

    @Provides
    @Spoofax3CompilerScope
    public UnarchiveFromJar provideUnarchiveFromJar() {
        return new UnarchiveFromJar();
    }

    @Provides
    @Spoofax3CompilerScope
    @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        Spoofax3LanguageProjectCompiler languageProjectCompiler,
        Spoofax3ParserLanguageCompiler parserLanguageCompiler,
        Spoofax3StylerLanguageCompiler stylerLanguageCompiler,
        Spoofax3ConstraintAnalyzerLanguageCompiler constraintAnalyzerCompiler,
        Spoofax3StrategoRuntimeLanguageCompiler strategoRuntimeLanguageCompiler,
        UnarchiveFromJar unarchiveFromJar
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(languageProjectCompiler);
        taskDefs.add(parserLanguageCompiler);
        taskDefs.add(stylerLanguageCompiler);
        taskDefs.add(constraintAnalyzerCompiler);
        taskDefs.add(strategoRuntimeLanguageCompiler);
        taskDefs.add(unarchiveFromJar);
        return taskDefs;
    }

    @Provides
    @Spoofax3CompilerScope
    @Spoofax3CompilerQualifier
    public Pie providePie(
        ResourceService resourceService,
        Set<TaskDef<?, ?>> taskDefs,
        @Platform PieBuilder pieBuilder,
        @Sdf3Qualifier Pie sdf3Pie,
        @StrategoQualifier Pie strategoPie,
        @EsvQualifier Pie esvPie,
        @StatixQualifier Pie statixPie,
        @LibSpoofax2Qualifier Pie libSpoofax2Pie,
        @LibStatixQualifier Pie libStatixPie
    ) {
        return pieBuilder.build()
            .createChildBuilder(sdf3Pie, strategoPie, esvPie, statixPie, libSpoofax2Pie, libStatixPie)
            .withTaskDefs(new MapTaskDefs(taskDefs))
            .withResourceService(resourceService)
            .build();
    }
}
