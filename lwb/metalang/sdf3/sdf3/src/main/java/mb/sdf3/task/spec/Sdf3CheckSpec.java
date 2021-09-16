package mb.sdf3.task.spec;

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
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.Sdf3ClassLoaderResources;
import mb.sdf3.Sdf3Scope;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.util.Sdf3Util;
import mb.stratego.common.InvalidAstShapeException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import java.util.stream.Stream;

@Sdf3Scope
public class Sdf3CheckSpec implements TaskDef<Sdf3SpecConfig, KeyedMessages> {
    private final Sdf3ClassLoaderResources classLoaderResources;
    private final Sdf3Parse parse;
    private final Sdf3AnalyzeMulti analyze;

    @Inject
    public Sdf3CheckSpec(Sdf3ClassLoaderResources classLoaderResources, Sdf3Parse parse, Sdf3AnalyzeMulti analyze) {
        this.classLoaderResources = classLoaderResources;
        this.parse = parse;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, Sdf3SpecConfig input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final ResourceWalker walker = Sdf3Util.createResourceWalker();
        final ResourceMatcher matcher = Sdf3Util.createResourceMatcher();
        final HierarchicalResource mainSourceDirectory = context.require(input.mainSourceDirectory, ResourceStampers.modifiedDirRec(walker, matcher));
        try(final Stream<? extends HierarchicalResource> stream = mainSourceDirectory.walk(walker, matcher)) {
            final JsglrParseTaskInput.Builder parseInputBuilder = parse.inputBuilder().rootDirectoryHint(input.rootDirectory);
            stream.forEach(file -> {
                final ResourcePath filePath = file.getPath();
                final Result<JsglrParseOutput, JsglrParseException> result = context.require(parseInputBuilder.withFile(filePath).buildSupplier());
                messagesBuilder.addMessages(filePath, result.mapOrElse(v -> v.messages.asMessages(), e -> e.getOptionalMessages().map(KeyedMessages::asMessages).orElseGet(Messages::of)));
                result.ifOk(output -> checkModuleName(filePath, output.ast, input, messagesBuilder));
            });
        }

        final Sdf3AnalyzeMulti.Input analyzeInput = new Sdf3AnalyzeMulti.Input(input.mainSourceDirectory, parse.createRecoverableMultiAstSupplierFunction(walker, matcher));
        final Result<Sdf3AnalyzeMulti.Output, ?> analysisResult = context.require(analyze, analyzeInput);
        analysisResult
            .ifOk(output -> {
                messagesBuilder.addMessages(output.result.messages);
                messagesBuilder.addMessages(output.messagesFromAstProviders);
            })
            .ifErr(e -> messagesBuilder.addMessage("SDF3 analysis failed", e, Severity.Error, input.mainSourceDirectory));
        return messagesBuilder.build();
    }

    private void checkModuleName(ResourcePath file, IStrategoTerm ast, Sdf3SpecConfig input, KeyedMessagesBuilder messagesBuilder) {
        final IStrategoTerm moduleNameTerm = TermUtils.asApplAt(ast, 0)
            .orElseThrow(() -> new InvalidAstShapeException("constructor application as first subterm", ast));
        final String moduleName = TermUtils.asJavaStringAt(moduleNameTerm, 0)
            .orElseThrow(() -> new InvalidAstShapeException("module name string as first subterm", moduleNameTerm));
        final String relativePath = input.mainSourceDirectory.relativize(file.removeLeafExtension());
        if(!moduleName.equals(relativePath)) {
            messagesBuilder.addMessage("Module name '" + moduleName + "' does not agree with relative file path '" +
                relativePath + "'. Either change the module name or move/rename the file", Severity.Error, file, TermTracer.getRegion(moduleNameTerm));
        }
    }
}
