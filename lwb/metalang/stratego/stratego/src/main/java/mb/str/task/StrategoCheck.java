package mb.str.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.str.StrategoScope;
import mb.str.config.StrategoAnalyzeConfig;
import mb.str.incr.MessageConverter;
import mb.str.task.spoofax.StrategoParseWrapper;
import mb.str.util.StrategoUtil;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.task.Check;
import mb.stratego.build.strincr.task.input.CheckInput;
import mb.stratego.build.strincr.task.output.CheckOutput;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;

@StrategoScope
public class StrategoCheck implements TaskDef<StrategoAnalyzeConfig, KeyedMessages> {
    private final ResourceService resourceService;
    private final StrategoParseWrapper strategoParse;
    private final Check check;

    @Inject
    public StrategoCheck(ResourceService resourceService, StrategoParseWrapper strategoParse, Check check) {
        this.resourceService = resourceService;
        this.strategoParse = strategoParse;
        this.check = check;
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
            final JsglrParseTaskInput.Builder parseInputBuilder = strategoParse.inputBuilder().rootDirectoryHint(config.rootDirectory);
            rootDirectory.walk(walker, matcher).forEach(file -> {
                final ResourcePath filePath = file.getPath();
                final Messages messages = context.require(parseInputBuilder.withFile(filePath).buildMessagesSupplier());
                messagesBuilder.addMessages(filePath, messages);
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }

        final CheckOutput output = context.require(check, new CheckInput(
            config.mainModule,
            config.rootDirectory,
            new IModuleImportService.ImportResolutionInfo(
                config.sourceFileOrigins.asCopy(),
                config.includeDirs.asCopy(),
                config.builtinLibs.asCopy(),
                config.str2libraries.asCopy()
            ),
            true
        ));
        MessageConverter.addMessagesToBuilder(messagesBuilder, output.messages, resourceService);

        return messagesBuilder.build(config.rootDirectory);
    }
}
