package mb.str.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.jsglr.common.TermTracer;
import mb.jsglr1.pie.JSGLR1ParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.str.StrategoScope;
import mb.str.config.StrategoAnalyzeConfig;
import mb.str.task.spoofax.StrategoParseWrapper;
import mb.str.util.StrategoUtil;
import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.StrIncrAnalysis;
import mb.stratego.build.strincr.message.Message;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;

@StrategoScope
public class StrategoCheck implements TaskDef<StrategoAnalyzeConfig, KeyedMessages> {
    private final ResourceService resourceService;
    private final StrategoParseWrapper strategoParse;
    private final StrIncrAnalysis analysis;

    @Inject
    public StrategoCheck(ResourceService resourceService, StrategoParseWrapper strategoParse, StrIncrAnalysis analysis) {
        this.resourceService = resourceService;
        this.strategoParse = strategoParse;
        this.analysis = analysis;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public KeyedMessages exec(ExecContext context, StrategoAnalyzeConfig config) throws IOException {
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        final ResourceWalker walker = StrategoUtil.createResourceWalker();
        final ResourceMatcher matcher = StrategoUtil.createResourceMatcher();
        final HierarchicalResource rootDirectory = context.require(config.rootDirectory, ResourceStampers.modifiedDirRec(walker, matcher));
        try {
            final JSGLR1ParseTaskInput.Builder parseInputBuilder = strategoParse.inputBuilder().rootDirectoryHint(config.rootDirectory);
            rootDirectory.walk(walker, matcher).forEach(file -> {
                final ResourcePath filePath = file.getPath();
                final Messages messages = context.require(parseInputBuilder.withFile(filePath).buildMessagesSupplier());
                messagesBuilder.addMessages(filePath, messages);
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        final Result<StrIncrAnalysis.Output, ?> result = Result.ofOkOrCatching(() -> context.require(analysis, new StrIncrAnalysis.Input(
            config.mainFile,
            config.includeDirs.asUnmodifiable(),
            config.builtinLibs.asUnmodifiable(),
            config.sourceFileOrigins.asUnmodifiable(),
            config.rootDirectory,
            config.gradualTypingSetting
        )));
        result.ifElse(
            output -> {
                for(Message<?> message : output.messages) {
                    final @Nullable Region region = TermTracer.getRegion(message.locationTerm);
                    final ResourceKey resourceKey = resourceService.getResourceKey(ResourceKeyString.parse(message.moduleFilePath));
                    final Severity severity = convertSeverity(message.severity);
                    messagesBuilder.addMessage(message.getMessage(), severity, resourceKey, region);
                }
            },
            ex -> messagesBuilder.addMessage("Stratego analysis failed unexpectedly", ex, Severity.Error, config.mainFile)
        );

        return messagesBuilder.build();
    }

    private static Severity convertSeverity(MessageSeverity severity) {
        switch(severity) {
            case NOTE:
                return Severity.Info;
            case WARNING:
                return Severity.Warning;
            case ERROR:
                return Severity.Error;
        }
        return Severity.Error;
    }
}
