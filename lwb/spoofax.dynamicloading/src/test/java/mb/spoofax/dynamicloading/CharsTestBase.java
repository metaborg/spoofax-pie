package mb.spoofax.dynamicloading;

import mb.common.option.Option;
import mb.common.util.Properties;
import mb.pie.api.ExecException;
import mb.pie.api.Task;
import mb.pie.runtime.tracer.MetricsTracer;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
import mb.spoofax.compiler.adapter.data.ArgProviderRepr;
import mb.spoofax.compiler.adapter.data.CommandDefRepr;
import mb.spoofax.compiler.adapter.data.ParamRepr;
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProject;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.arg.ArgConverters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

class CharsTestBase extends TestBase {
    LanguageProjectCompiler.Input languageProjectInput;
    AdapterProjectCompiler.Input adapterProjectInput;
    CompileToJavaClassFiles.Input input;
    ResourcePath charsFilePath;
    HierarchicalResource charsFile;

    void setup(Path temporaryDirectoryPath) throws IOException {
        super.setup(temporaryDirectoryPath);

        copyResourcesToTemporaryDirectory("mb/spoofax/dynamicloading/chars");

        final String packageId = "mb.chars";
        final Shared shared = Shared.builder()
            .name("Chars")
            .defaultPackageId(packageId)
            .defaultClassPrefix("Chars")
            .build();

        final LanguageProject languageProject = LanguageProject.builder().withDefaults(temporaryDirectory.getPath(), shared).build();
        final Spoofax3LanguageProject spoofax3LanguageProject = Spoofax3LanguageProject.builder().languageProject(languageProject).build();
        final AdapterProject adapterProject = AdapterProject.builder().withDefaults(temporaryDirectory.getPath(), shared).build();

        final LanguageProjectCompilerInputBuilder languageProjectInputBuilder = new LanguageProjectCompilerInputBuilder();
        final Spoofax3LanguageProjectCompilerInputBuilder spoofax3LanguageProjectInputBuilder = new Spoofax3LanguageProjectCompilerInputBuilder();
        final AdapterProjectCompilerInputBuilder adapterProjectCompilerInputBuilder = new AdapterProjectCompilerInputBuilder();

        spoofax3LanguageProjectInputBuilder.withParser();
        spoofax3LanguageProjectInputBuilder.withStyler();
        spoofax3LanguageProjectInputBuilder.withStrategoRuntime();
        final Spoofax3LanguageProjectCompiler.Input spoofax3LanguageProjectInput = spoofax3LanguageProjectInputBuilder.build(new Properties(), shared, spoofax3LanguageProject);
        spoofax3LanguageProjectInput.syncTo(languageProjectInputBuilder);

        languageProjectInputBuilder.withParser().startSymbol("Start");
        languageProjectInputBuilder.withStyler();
        languageProjectInputBuilder.withStrategoRuntime();
        this.languageProjectInput = languageProjectInputBuilder.build(shared, languageProject);

        adapterProjectCompilerInputBuilder.withParser();
        adapterProjectCompilerInputBuilder.withStyler();
        adapterProjectCompilerInputBuilder.withStrategoRuntime();

        final TypeInfo removeA = TypeInfo.of(packageId, "CharsRemoveA");
        final TypeInfo debugRemoveA = TypeInfo.of(packageId, "CharsDebugRemoveA");
        adapterProjectCompilerInputBuilder.project.addTaskDefs(removeA, debugRemoveA);
        final CommandDefRepr debugRemoveACommand = CommandDefRepr.builder()
            .type(packageId, "CharsDebugRemoveACommand")
            .taskDefType(debugRemoveA)
            .argType(debugRemoveA.appendToId(".Args"))
            .displayName("Show AST with A characters removed")
            .description("Shows the AST with A characters removed")
            .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
            .addParams(
                ParamRepr.of("file", TypeInfo.of("mb.resource", "ResourceKey"), true, ArgProviderRepr.context(CommandContextType.File))
            )
            .build();
        adapterProjectCompilerInputBuilder.project.addCommandDefs(debugRemoveACommand);

        this.adapterProjectInput = adapterProjectCompilerInputBuilder.build(languageProjectInput, Option.ofNone(), adapterProject);

        this.input = CompileToJavaClassFiles.Input.builder()
            .languageProjectInput(languageProjectInput)
            .spoofax3LanguageProjectInput(spoofax3LanguageProjectInput)
            .adapterProjectInput(adapterProjectInput)
            .build();

        this.charsFile = temporaryDirectory.appendSegment("test.chars").ensureFileExists();
        this.charsFilePath = charsFile.getPath();
        charsFile.writeString("abcdefg");
    }

    void teardown() throws Exception {
        charsFile = null;
        input = null;
        adapterProjectInput = null;
        languageProjectInput = null;
        super.teardown();
    }


    Task<CommandFeedback> getTaskForFirstCommand(LanguageInstance languageInstance) {
        final Optional<CommandDef<?>> debugRemoveACommandOption = languageInstance.getCommandDefs().stream().findFirst();
        final CommandDef<?> debugRemoveACommand = debugRemoveACommandOption.get();
        return debugRemoveACommand
            .request(CommandExecutionType.ManualOnce)
            .createTask(CommandContext.ofFile(charsFilePath), new ArgConverters(resourceService));
    }


    boolean hasTokenizeTaskDefExecuted(MetricsTracer.Report report) {
        return report.executedPerTaskDefinition.containsKey(adapterProjectInput.parser().get().tokenizeTaskDef().qualifiedId());
    }

    boolean hasParseTaskDefExecuted(MetricsTracer.Report report) {
        return report.executedPerTaskDefinition.containsKey(adapterProjectInput.parser().get().parseTaskDef().qualifiedId());
    }

    boolean hasStyleTaskDefExecuted(MetricsTracer.Report report) {
        return report.executedPerTaskDefinition.containsKey(adapterProjectInput.styler().get().styleTaskDef().qualifiedId());
    }

    boolean hasRemoveATaskDefExecuted(MetricsTracer.Report report) {
        return report.executedPerTaskDefinition.containsKey("mb.chars.CharsRemoveA");
    }

    boolean hasDebugRemoveATaskDefExecuted(MetricsTracer.Report report) {
        return report.executedPerTaskDefinition.containsKey("mb.chars.CharsDebugRemoveA");
    }


    DynamicLoaderReloadSession modifyStyler(DynamicLoaderMixedSession session) throws IOException, ExecException, InterruptedException {
        final ResourcePath path = input.spoofax3LanguageProjectInput().styler().get().esvMainFile();
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString().replace("0 0 150 bold", "255 255 0 italic");
        file.writeString(text);
        return session.updateAffectedBy(path);
    }

    DynamicLoaderReloadSession modifyParser(DynamicLoaderMixedSession session) throws IOException, ExecException, InterruptedException {
        final ResourcePath path = input.spoofax3LanguageProjectInput().parser().get().sdf3MainFile();
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString().replace("\\ ", "\\ \\t");
        file.writeString(text);
        return session.updateAffectedBy(path);
    }

    DynamicLoaderReloadSession modifyTransformation(DynamicLoaderMixedSession session) throws IOException, ExecException, InterruptedException {
        final ResourcePath path = input.spoofax3LanguageProjectInput().strategoRuntime().get().strategoRootDirectory().appendRelativePath("transform.str");
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString().replace("string-replace(|\"a\", \"a\")", "string-replace(|\"a\", \"\")");
        file.writeString(text);
        return session.updateAffectedBy(path);
    }
}
