package mb.spoofax.lwb.dynamicloading;

import mb.pie.api.ExecException;
import mb.pie.api.Task;
import mb.pie.runtime.tracer.MetricsTracer;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.command.CommandContext;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.arg.ArgConverters;
import mb.spoofx.lwb.compiler.cfg.CompileLanguageToJavaClassPathInput;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

class CharsTestBase extends TestBase {
    ResourcePath rootDirectory;
    HierarchicalResource charsProject;
    ResourcePath charsProjectPath;
    HierarchicalResource charsFile;
    ResourcePath charsFilePath;

    void setup(Path temporaryDirectoryPath) throws IOException {
        super.setup(temporaryDirectoryPath);
        copyResourcesToTemporaryDirectory("mb/spoofax/lwb/dynamicloading/chars");
        this.rootDirectory = temporaryDirectory.getPath();
        this.charsProject = temporaryDirectory.appendRelativePath("test");
        this.charsProjectPath = charsProject.getPath();
        this.charsFile = charsProject.appendSegment("test.chars").ensureFileExists();
        this.charsFilePath = charsFile.getPath();
        charsFile.writeString("abcdefg");
    }

    void teardown() throws Exception {
        charsFilePath = null;
        charsFile = null;
        charsProjectPath = null;
        charsProject = null;
        rootDirectory = null;
        super.teardown();
    }


    Task<CommandFeedback> getTaskForFirstCommand(LanguageInstance languageInstance) {
        final Optional<CommandDef<?>> debugRemoveACommandOption = languageInstance.getCommandDefs().stream().findFirst();
        final CommandDef<?> debugRemoveACommand = debugRemoveACommandOption.get();
        return debugRemoveACommand
            .request(CommandExecutionType.ManualOnce)
            .createTask(CommandContext.ofFile(charsFilePath), new ArgConverters(resourceService));
    }


    boolean hasTokenizeTaskDefExecuted(MetricsTracer.Report report, DynamicLanguage language) {
        return report.hasTaskDefExecuted(language.getCompileInput().adapterProjectInput().parser().get().tokenizeTaskDef().qualifiedId());
    }

    boolean hasParseTaskDefExecuted(MetricsTracer.Report report, DynamicLanguage language) {
        return report.hasTaskDefExecuted(language.getCompileInput().adapterProjectInput().parser().get().parseTaskDef().qualifiedId());
    }

    boolean hasStyleTaskDefExecuted(MetricsTracer.Report report, DynamicLanguage language) {
        return report.hasTaskDefExecuted(language.getCompileInput().adapterProjectInput().styler().get().styleTaskDef().qualifiedId());
    }

    boolean hasRemoveATaskDefExecuted(MetricsTracer.Report report) {
        return report.hasTaskDefExecuted("mb.chars.CharsRemoveA");
    }

    boolean hasDebugRemoveATaskDefExecuted(MetricsTracer.Report report) {
        return report.hasTaskDefExecuted("mb.chars.CharsDebugRemoveA");
    }

    boolean hasConstraintAnalysisTaskExecuted(MetricsTracer.Report report, DynamicLanguage language) {
        return report.hasTaskDefExecuted(language.getCompileInput().adapterProjectInput().constraintAnalyzer().get().analyzeTaskDef().qualifiedId());
    }

    boolean hasCheckTaskExecuted(MetricsTracer.Report report, DynamicLanguage language) {
        return report.hasTaskDefExecuted(language.getCompileInput().adapterProjectInput().checkTaskDef().qualifiedId());
    }


    DynamicLoaderReloadSession modifyStyler(DynamicLoaderMixedSession session, CompileLanguageToJavaClassPathInput input) throws IOException, ExecException, InterruptedException {
        final ResourcePath path = input.compileLanguageInput().esv().get().esvMainFile();
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString().replace("0 0 150 bold", "255 255 0 italic");
        file.writeString(text);
        return session.updateAffectedBy(path);
    }

    DynamicLoaderReloadSession modifyParser(DynamicLoaderMixedSession session, CompileLanguageToJavaClassPathInput input) throws IOException, ExecException, InterruptedException {
        final ResourcePath path = input.compileLanguageInput().sdf3().get().sdf3MainFile();
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString().replace("\\ ", "\\ \\t");
        file.writeString(text);
        return session.updateAffectedBy(path);
    }

    DynamicLoaderReloadSession modifyTransformation(DynamicLoaderMixedSession session, CompileLanguageToJavaClassPathInput input) throws IOException, ExecException, InterruptedException {
        final ResourcePath path = input.compileLanguageInput().stratego().get().strategoRootDirectory().appendRelativePath("transform/remove-a.str");
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString().replace("string-replace(|\"a\", \"a\")", "string-replace(|\"a\", \"\")");
        file.writeString(text);
        return session.updateAffectedBy(path);
    }

    DynamicLoaderReloadSession modifyCommand(DynamicLoaderMixedSession session, CompileLanguageToJavaClassPathInput input) throws IOException, ExecException, InterruptedException {
        final ResourcePath path = input.javaSourcePath().get(0).appendRelativePath("mb/chars/CharsDebugRemoveA.java");
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString().replace("A characters", "'A' characters");
        file.writeString(text);
        return session.updateAffectedBy(path);
    }

    DynamicLoaderReloadSession modifyAnalyzer(DynamicLoaderMixedSession session, CompileLanguageToJavaClassPathInput input) throws IOException, ExecException, InterruptedException {
        final ResourcePath path = input.compileLanguageInput().statix().get().statixMainFile();
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString()
            .replace("Chars(\"\")", "Chars(\"abcdefg\")")
            .replace("combination '' is", "combination 'abcdefg' is");
        file.writeString(text);
        return session.updateAffectedBy(path);
    }
}
