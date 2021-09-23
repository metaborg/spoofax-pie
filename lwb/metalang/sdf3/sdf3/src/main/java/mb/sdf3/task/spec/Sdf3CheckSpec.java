package mb.sdf3.task.spec;

import mb.aterm.common.InvalidAstShapeException;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr.common.TermTracer;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.util.SeparatorUtil;
import mb.sdf3.Sdf3ClassLoaderResources;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3GetSourceFiles;
import mb.sdf3.task.Sdf3GetStrategoRuntimeProvider;
import mb.sdf3.task.Sdf3Parse;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoTermMessageCollector;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;

@Sdf3Scope
public class Sdf3CheckSpec implements TaskDef<Sdf3SpecConfig, KeyedMessages> {
    private final Sdf3ClassLoaderResources classLoaderResources;
    private final Sdf3Parse parse;
    private final Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider;
    private final Sdf3GetSourceFiles getSourceFiles;
    private final Sdf3AnalyzeMulti analyze;

    @Inject
    public Sdf3CheckSpec(
        Sdf3ClassLoaderResources classLoaderResources,
        Sdf3Parse parse,
        Sdf3GetStrategoRuntimeProvider getStrategoRuntimeProvider,
        Sdf3GetSourceFiles getSourceFiles,
        Sdf3AnalyzeMulti analyze
    ) {
        this.classLoaderResources = classLoaderResources;
        this.parse = parse;
        this.getStrategoRuntimeProvider = getStrategoRuntimeProvider;
        this.getSourceFiles = getSourceFiles;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, Sdf3SpecConfig input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProvider, None.instance).getValue().get();

        final JsglrParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().rootDirectoryHint(input.rootDirectory);
        for(ResourcePath file : context.require(getSourceFiles, input.rootDirectory)) {
            final Result<JsglrParseOutput, JsglrParseException> result = context.require(parseInputBuilder.withFile(file).buildSupplier());
            messagesBuilder.addMessages(file, result.mapOrElse(v -> v.messages.asMessages(), e -> e.getOptionalMessages().map(KeyedMessages::asMessages).orElseGet(Messages::of)));
            result.ifOk(output -> {
                checkModuleName(file, output.ast, input, messagesBuilder);
                collectMessagesFromStrategoStrategy(file, output.ast, strategoRuntime, messagesBuilder);
            });
        }

        final Sdf3AnalyzeMulti.Input analyzeInput = new Sdf3AnalyzeMulti.Input(input.rootDirectory, parse.createRecoverableMultiAstSupplierFunction(getSourceFiles.createFunction()));
        final Result<Sdf3AnalyzeMulti.Output, ?> analysisResult = context.require(analyze, analyzeInput);
        analysisResult
            .ifOk(output -> {
                messagesBuilder.addMessages(output.result.messages);
                messagesBuilder.addMessages(output.messagesFromAstProviders);
            })
            .ifErr(e -> messagesBuilder.addMessage("SDF3 analysis failed", e, Severity.Error, input.mainSourceDirectory));
        return messagesBuilder.build();
    }

    private void checkModuleName(
        ResourcePath file,
        IStrategoTerm ast,
        Sdf3SpecConfig input,
        KeyedMessagesBuilder messagesBuilder
    ) {
        final IStrategoTerm moduleNameTerm = TermUtils.asApplAt(ast, 0)
            .orElseThrow(() -> new InvalidAstShapeException("constructor application as first subterm", ast));
        final String moduleName = SeparatorUtil.convertCurrentToUnixSeparator(TermUtils.asJavaStringAt(moduleNameTerm, 0)
            .orElseThrow(() -> new InvalidAstShapeException("module name string as first subterm", moduleNameTerm)));
        final String relativePath = SeparatorUtil.convertCurrentToUnixSeparator(input.mainSourceDirectory.relativize(file.removeLeafExtension()));
        if(!moduleName.equals(relativePath)) {
            messagesBuilder.addMessage("Module name '" + moduleName + "' does not agree with relative file path '" +
                relativePath + "'. Either change the module name or move/rename the file", Severity.Error, file, TermTracer.getRegion(moduleNameTerm));
        }
    }

    private void collectMessagesFromStrategoStrategy(
        ResourcePath file,
        IStrategoTerm ast,
        StrategoRuntime strategoRuntime,
        KeyedMessagesBuilder messagesBuilder
    ) {
        try {
            final IStrategoTerm messagesTerm = strategoRuntime.invoke("spoofax3-check", ast);
            StrategoTermMessageCollector.addTermMessages(messagesTerm, file, messagesBuilder);
        } catch(StrategoException e) {
            messagesBuilder.addMessage("Failed to run Stratego-based checks on SDF3 file", e, Severity.Error, file);
        }
    }
}
