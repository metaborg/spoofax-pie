package mb.spoofax.lwb.compiler;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.cfg.CompileLanguageInput;
import mb.pie.api.TaskDef;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.pie.task.java.CompileJava;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.lwb.compiler.cfg.SpoofaxCfgCheck;
import mb.spoofax.lwb.compiler.definition.CheckLanguageDefinition;
import mb.spoofax.lwb.compiler.definition.CompileLanguageDefinition;
import mb.spoofax.lwb.compiler.definition.CompileMetaLanguageSources;
import mb.spoofax.lwb.compiler.dynamix.SpoofaxDynamixCheck;
import mb.spoofax.lwb.compiler.dynamix.SpoofaxDynamixCompile;
import mb.spoofax.lwb.compiler.dynamix.SpoofaxDynamixConfigure;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvCheck;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvCompile;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvConfigure;
import mb.spoofax.lwb.compiler.esv.SpoofaxEsvResolveDependencies;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Check;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Compile;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3Configure;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3ResolveDependencies;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixCheck;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixCompile;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixConfigure;
import mb.spoofax.lwb.compiler.statix.SpoofaxStatixResolveDependencies;
import mb.spoofax.lwb.compiler.stratego.SpoofaxStrategoCheck;
import mb.spoofax.lwb.compiler.stratego.SpoofaxStrategoCompile;
import mb.spoofax.lwb.compiler.stratego.SpoofaxStrategoConfigure;
import mb.spoofax.lwb.compiler.stratego.SpoofaxStrategoResolveDependencies;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@Module
public class SpoofaxLwbCompilerModule {
    private final TemplateCompiler templateCompiler;
    private Function<CompileLanguageInput, String> participantClassQualifiedIdSelector = input -> input.adapterProjectInput().participant().qualifiedId();

    public SpoofaxLwbCompilerModule(TemplateCompiler templateCompiler) {
        this.templateCompiler = templateCompiler;
    }


    public SpoofaxLwbCompilerModule setParticipantClassQualifiedIdSelector(Function<CompileLanguageInput, String> participantClassQualifiedIdSelector) {
        this.participantClassQualifiedIdSelector = participantClassQualifiedIdSelector;
        return this;
    }


    @Provides @SpoofaxLwbCompilerScope
    TemplateCompiler provideTemplateCompiler() {
        return templateCompiler;
    }

    @Provides @SpoofaxLwbCompilerScope
    Function<CompileLanguageInput, String> provideParticipantClassQualifiedIdSelector() {
        return participantClassQualifiedIdSelector;
    }


    @Provides @SpoofaxLwbCompilerScope
    static UnarchiveFromJar provideUnarchiveFromJar() {
        return new UnarchiveFromJar();
    }


    @Provides @SpoofaxLwbCompilerQualifier @SpoofaxLwbCompilerScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        CompileLanguageDefinition compileLanguageDefinition,
        CheckLanguageDefinition checkLanguageDefinition,
        CompileMetaLanguageSources compileMetaLanguageSources,

        SpoofaxCfgCheck spoofaxCfgCheck,

        SpoofaxSdf3ResolveDependencies spoofaxSdf3ResolveDependencies,
        SpoofaxSdf3Configure spoofaxSdf3Configure,
        SpoofaxSdf3Check spoofaxSdf3Check,
        SpoofaxSdf3Compile spoofaxSdf3Compile,

        SpoofaxEsvResolveDependencies spoofaxEsvResolveDependencies,
        SpoofaxEsvConfigure spoofaxEsvConfigure,
        SpoofaxEsvCheck spoofaxEsvCheck,
        SpoofaxEsvCompile spoofaxEsvCompile,

        SpoofaxStatixResolveDependencies spoofaxStatixResolveDependencies,
        SpoofaxStatixConfigure spoofaxStatixConfigure,
        SpoofaxStatixCheck spoofaxStatixCheck,
        SpoofaxStatixCompile spoofaxStatixCompile,

        SpoofaxDynamixConfigure spoofaxDynamixConfigure,
        SpoofaxDynamixCheck spoofaxDynamixCheck,
        SpoofaxDynamixCompile spoofaxDynamixCompile,

        SpoofaxStrategoResolveDependencies spoofaxStrategoResolveDependencies,
        SpoofaxStrategoConfigure spoofaxStrategoConfigure,
        SpoofaxStrategoCheck spoofaxStrategoCheck,
        SpoofaxStrategoCompile spoofaxStrategoCompile,

        CompileJava compileJava,
        UnarchiveFromJar unarchiveFromJar
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(compileLanguageDefinition);
        taskDefs.add(checkLanguageDefinition);
        taskDefs.add(compileMetaLanguageSources);

        taskDefs.add(spoofaxCfgCheck);

        taskDefs.add(spoofaxSdf3ResolveDependencies);
        taskDefs.add(spoofaxSdf3Configure);
        taskDefs.add(spoofaxSdf3Check);
        taskDefs.add(spoofaxSdf3Compile);

        taskDefs.add(spoofaxEsvResolveDependencies);
        taskDefs.add(spoofaxEsvConfigure);
        taskDefs.add(spoofaxEsvCheck);
        taskDefs.add(spoofaxEsvCompile);

        taskDefs.add(spoofaxStatixResolveDependencies);
        taskDefs.add(spoofaxStatixConfigure);
        taskDefs.add(spoofaxStatixCheck);
        taskDefs.add(spoofaxStatixCompile);

        taskDefs.add(spoofaxDynamixConfigure);
        taskDefs.add(spoofaxDynamixCheck);
        taskDefs.add(spoofaxDynamixCompile);

        taskDefs.add(spoofaxStrategoResolveDependencies);
        taskDefs.add(spoofaxStrategoConfigure);
        taskDefs.add(spoofaxStrategoCheck);
        taskDefs.add(spoofaxStrategoCompile);

        taskDefs.add(compileJava);
        taskDefs.add(unarchiveFromJar);
        return taskDefs;
    }
}
