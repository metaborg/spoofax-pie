package mb.tiger.spoofax.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.tiger.spoofax.TigerScope;
import mb.tiger.spoofax.task.reusable.TigerAnalyzeMulti;
import mb.tiger.spoofax.task.reusable.TigerParse;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @implNote Although Tiger is a single-file language, we implement the multi-file check variant here as well for
 * development/testing purposes.
 */
@TigerScope
public class TigerCheckMulti implements TaskDef<ResourcePath, KeyedMessages> {
    private final TigerParse parse;
    private final TigerAnalyzeMulti analyze;

    @Inject public TigerCheckMulti(TigerParse parse, TigerAnalyzeMulti analyze) {
        this.parse = parse;
        this.analyze = analyze;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public KeyedMessages exec(ExecContext context, ResourcePath input) throws IOException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final ResourceWalker walker = ResourceWalker.ofPath(PathMatcher.ofNoHidden());
        final HierarchicalResource rootDirectory = context.getHierarchicalResource(input);
        // Require directories recursively, so we re-execute whenever a file is added/removed from a directory.
        rootDirectory.walkForEach(walker, ResourceMatcher.ofDirectory(), context::require);
        final ResourceMatcher matcher = ResourceMatcher.ofFile().and(ResourceMatcher.ofPath(PathMatcher.ofExtensions("tig")));
        rootDirectory.walkForEach(walker, matcher, file -> {
            final ResourcePath filePath = file.getPath();
            final Messages messages = context.require(parse.inputBuilder().withFile(filePath).rootDirectoryHint(input).buildMessagesSupplier());
            messagesBuilder.addMessages(filePath, messages);
        });
        final TigerAnalyzeMulti.Input analyzeInput = new TigerAnalyzeMulti.Input(input, parse.createRecoverableMultiAstSupplierFunction(walker, matcher));
        final Result<TigerAnalyzeMulti.Output, ?> analysisResult = context.require(analyze, analyzeInput);
        analysisResult
            .ifOk(output -> {
                messagesBuilder.addMessages(output.result.messages);
                messagesBuilder.addMessages(output.messagesFromAstProviders);
            })
            .ifErr(e -> messagesBuilder.addMessage("Project-wide analysis failed", e, Severity.Error, input));
        return messagesBuilder.build();
    }
}
