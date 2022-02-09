package mb.spoofax.lwb.dynamicloading;

import mb.cfg.CompileLanguageInput;
import mb.cfg.metalang.CfgEsvSource;
import mb.cfg.metalang.CfgSdf3Source;
import mb.cfg.metalang.CfgStatixSource;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Task;
import mb.pie.api.TopDownSession;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

class CharsTestBase extends TestBase {
    ResourcePath rootDirectoryPath;
    HierarchicalResource charsProject;
    ResourcePath charsProjectPath;
    HierarchicalResource charsFile;
    ResourcePath charsFilePath;

    void setup(Path temporaryDirectoryPath) throws IOException {
        super.setup(temporaryDirectoryPath);
        copyResourcesToTemporaryDirectory("mb/spoofax/lwb/dynamicloading/chars");
        rootDirectoryPath = rootDirectory.getPath();
        charsProject = rootDirectory.appendRelativePath("test");
        charsProjectPath = charsProject.getPath();
        charsFile = charsProject.appendSegment("test.chars").ensureFileExists();
        charsFilePath = charsFile.getPath();
        charsFile.writeString("abcdefg");
    }

    void teardown() throws Exception {
        charsFilePath = null;
        charsFile = null;
        charsProjectPath = null;
        charsProject = null;
        rootDirectoryPath = null;
        super.teardown();
    }


    Task<CommandFeedback> getTaskForRemoveACommand(LanguageInstance languageInstance) {
        final Optional<CommandDef<?>> debugRemoveACommandOption = languageInstance.getCommandDefs().stream().filter(cd -> cd.getDisplayName().contains("Show AST with 'A' characters removed")).findFirst();
        final CommandDef<?> debugRemoveACommand = debugRemoveACommandOption.get();
        return debugRemoveACommand
            .request(CommandExecutionType.ManualOnce)
            .createTask(CommandContext.ofFile(charsFilePath), new ArgConverters(resourceService));
    }


    boolean hasTokenizeTaskDefExecuted(MetricsTracer.Report report, CompileLanguageInput input) {
        return report.hasTaskDefExecuted(input.adapterProjectInput().parser().get().tokenizeTaskDef().qualifiedId());
    }

    boolean hasParseTaskDefExecuted(MetricsTracer.Report report, CompileLanguageInput input) {
        return report.hasTaskDefExecuted(input.adapterProjectInput().parser().get().parseTaskDef().qualifiedId());
    }

    boolean hasStyleTaskDefExecuted(MetricsTracer.Report report, CompileLanguageInput input) {
        return report.hasTaskDefExecuted(input.adapterProjectInput().styler().get().styleTaskDef().qualifiedId());
    }

    boolean hasRemoveATaskDefExecuted(MetricsTracer.Report report) {
        return report.hasTaskDefExecuted("mb.chars.CharsRemoveA");
    }

    boolean hasDebugRemoveATaskDefExecuted(MetricsTracer.Report report) {
        return report.hasTaskDefExecuted("mb.chars.CharsDebugRemoveA");
    }

    boolean hasConstraintAnalysisTaskExecuted(MetricsTracer.Report report, CompileLanguageInput input) {
        return report.hasTaskDefExecuted(input.adapterProjectInput().constraintAnalyzer().get().analyzeTaskDef().qualifiedId());
    }

    boolean hasCheckTaskExecuted(MetricsTracer.Report report, CompileLanguageInput input) {
        return report.hasTaskDefExecuted(input.adapterProjectInput().checkTaskDef().qualifiedId());
    }

    boolean hasHoverTaskExecuted(MetricsTracer.Report report, CompileLanguageInput input) {
        return report.hasTaskDefExecuted(input.adapterProjectInput().hover().get().hoverTaskDef().qualifiedId());
    }


    TopDownSession modifyStyler(MixedSession session, CompileLanguageInput input) throws IOException, ExecException, InterruptedException {
        final CfgEsvSource.Files files = input.compileLanguageSpecificationInput().esv().get().source().getFiles().orElseThrow(() -> new RuntimeException("Not using source files with ESV"));
        final ResourcePath path = files.mainFile();
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString().replace("0 0 150 bold", "255 255 0 italic");
        file.writeString(text);
        return session.updateAffectedBy(Collections.singleton(path));
    }

    TopDownSession modifyParser(MixedSession session, CompileLanguageInput input) throws IOException, ExecException, InterruptedException {
        final CfgSdf3Source.Files files = input.compileLanguageSpecificationInput().sdf3().get().source().getFiles().orElseThrow(() -> new RuntimeException("Not using source files with SDF3"));
        final ResourcePath path = files.mainFile();
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString().replace("\\ ", "\\ \\t");
        file.writeString(text);
        return session.updateAffectedBy(Collections.singleton(path));
    }

    TopDownSession modifyTransformation(MixedSession session, CompileLanguageInput input) throws IOException, ExecException, InterruptedException {
        final ResourcePath path = input.compileLanguageSpecificationInput().stratego().get().source().getFiles().mainSourceDirectory().appendRelativePath("transform/remove-a.str2");
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString().replace("string-replace(|\"a\", \"a\")", "string-replace(|\"a\", \"\")");
        file.writeString(text);
        return session.updateAffectedBy(Collections.singleton(path));
    }

    TopDownSession modifyCommand(MixedSession session, CompileLanguageInput input) throws IOException, ExecException, InterruptedException {
        final ResourcePath path = input.userJavaSourcePaths().get(0).appendRelativePath("mb/chars/CharsDebugRemoveA.java");
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString().replace("A characters", "'A' characters");
        file.writeString(text);
        return session.updateAffectedBy(Collections.singleton(path));
    }

    TopDownSession modifyAnalyzer(MixedSession session, CompileLanguageInput input) throws IOException, ExecException, InterruptedException {
        final CfgStatixSource.Files files = input.compileLanguageSpecificationInput().statix().get().source().getFiles().orElseThrow(() -> new RuntimeException("Not using source files with Statix"));
        final ResourcePath path = files.mainFile();
        final WritableResource file = resourceService.getWritableResource(path);
        final String text = file.readString()
            .replace("Chars(\"\")", "Chars(\"abcdefg\")")
            .replace("combination '' is", "combination 'abcdefg' is");
        file.writeString(text);
        return session.updateAffectedBy(Collections.singleton(path));
    }
}
