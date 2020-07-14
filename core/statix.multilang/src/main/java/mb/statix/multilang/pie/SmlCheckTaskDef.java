package mb.statix.multilang.pie;

import dagger.Lazy;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.pie.config.SmlBuildContextConfiguration;

import java.io.IOException;
import java.util.List;

public abstract class SmlCheckTaskDef implements TaskDef<ResourcePath, KeyedMessages>  {
    private final Function<ResourceKey, Messages> parseMessageFunction;
    private final SmlBuildContextConfiguration buildContextConfiguration;
    private final SmlBuildMessages buildMessages;
    private final Lazy<AnalysisContextService> analysisContextService;

    public SmlCheckTaskDef(
        Function<ResourceKey, Messages> parseMessageFunction,
        SmlBuildContextConfiguration buildContextConfiguration,
        SmlBuildMessages buildMessages,
        Lazy<AnalysisContextService> analysisContextService
    ) {
        this.parseMessageFunction = parseMessageFunction;
        this.buildContextConfiguration = buildContextConfiguration;
        this.buildMessages = buildMessages;
        this.analysisContextService = analysisContextService;
    }

    @Override
    public KeyedMessages exec(ExecContext context, ResourcePath projectPath) {
        // Aggregate all parse messages
        final KeyedMessagesBuilder builder = new KeyedMessagesBuilder();
        analysisContextService.get().getLanguageMetadataResult(getLanguageId())
            .ifElse(
                languageMetadata -> languageMetadata
                    .resourcesSupplier()
                    .apply(context, projectPath)
                    .forEach(resourceKey -> {
                        try {
                            Messages messages = context.require(parseMessageFunction.createSupplier(resourceKey));
                            builder.addMessages(resourceKey, messages);
                        } catch(IOException e) {
                            builder.addMessage("IO Exception when parsing file", e, Severity.Error, resourceKey);
                        }
                    }),
                err -> builder.addMessages(err.toKeyedMessages())
            );

        // Aggregate all Analysis Messages
        return context.require(buildContextConfiguration.createTask(new SmlBuildContextConfiguration.Input(projectPath, getLanguageId())))
            .mapErr(MultiLangAnalysisException::wrapIfNeeded)
            .flatMap(contextInfo -> {
                final List<LanguageId> languageIds = contextInfo.getContextConfig().getLanguages();
                try {
                    // We execute the actual analysis in the context of a shared Pie, to make sure all information is present.
                    // This will not break incrementality, because this task (or its equivalents for other languages)
                    // depends directly on all the files SmlBuildMessages depends on:
                    // - language source files:     via parse tasks
                    // - multilang.yaml:            via buildContextConfiguration tasks
                    final Pie sharedPie = analysisContextService.get().buildPieForLanguages(languageIds);
                    try(MixedSession session = sharedPie.newSession()) {
                        final Task<KeyedMessages> messagesTask = buildMessages.createTask(new SmlBuildMessages.Input(
                            projectPath,
                            languageIds,
                            contextInfo.getContextConfig().parseLevel()
                        ));
                        return TaskUtils.executeWrapped(() -> Result.ofOk(session.require(messagesTask)), "Exception executing analysis");
                    }
                } catch(MultiLangAnalysisException e) {
                    return Result.ofErr(e);
                }
            })
            .mapOrElse((KeyedMessages messages) -> {
                builder.addMessages(messages);
                return builder.build();
            }, MultiLangAnalysisException::toKeyedMessages);
    }

    protected abstract LanguageId getLanguageId();
}
