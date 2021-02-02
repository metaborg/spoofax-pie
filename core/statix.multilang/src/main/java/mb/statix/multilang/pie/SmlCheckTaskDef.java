package mb.statix.multilang.pie;

import dagger.Lazy;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.ConfigurationException;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.metadata.FileResult;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.metadata.LanguageMetadata;
import mb.statix.multilang.metadata.LanguageMetadataManager;
import mb.statix.multilang.pie.config.ContextConfig;
import mb.statix.multilang.pie.config.SmlBuildContextConfiguration;
import mb.statix.multilang.utils.MessageUtils;
import mb.statix.solver.IState;
import mb.statix.solver.persistent.SolverResult;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class SmlCheckTaskDef implements TaskDef<ResourcePath, KeyedMessages> {
    private final Function<ResourceKey, Messages> parseMessageFunction;
    private final SmlBuildContextConfiguration buildContextConfiguration;
    private final SmlSolveProject solveProject;
    private final Lazy<LanguageMetadataManager> languageMetadataManager;

    public SmlCheckTaskDef(
        Function<ResourceKey, Messages> parseMessageFunction,
        SmlBuildContextConfiguration buildContextConfiguration,
        SmlSolveProject solveProject,
        Lazy<LanguageMetadataManager> languageMetadataManager
    ) {
        this.parseMessageFunction = parseMessageFunction;
        this.buildContextConfiguration = buildContextConfiguration;
        this.solveProject = solveProject;
        this.languageMetadataManager = languageMetadataManager;
    }

    @Override
    public KeyedMessages exec(ExecContext context, ResourcePath projectPath) {
        // Aggregate all parse messages
        final KeyedMessagesBuilder builder = new KeyedMessagesBuilder();
        final Result<HashSet<ResourceKey>, MultiLangAnalysisException> resourceKeysResult = languageMetadataManager.get()
            .getLanguageMetadataResult(getLanguageId())
            .map(LanguageMetadata::resourcesSupplier)
            .map(supplier -> supplier.apply(context, projectPath))
            .ifOk(resourceKeys -> builder.addMessages(getParseMessages(context, resourceKeys)))
            .ifErr(err -> builder.addMessages(err.toKeyedMessages()));

        // Aggregate all Analysis Messages
        Task<Result<ContextConfig, ConfigurationException>> configTask = buildContextConfiguration
            .createTask(new SmlBuildContextConfiguration.Input(projectPath, getLanguageId()));

        context.require(configTask)
            .mapErr(MultiLangAnalysisException::wrapIfNeeded)
            .flatMap(contextInfo -> context.require(solveProject
                .createTask(new SmlSolveProject.Input(projectPath, new HashSet<>(contextInfo.languages()), contextInfo.parseLevel())))
                .flatMap(results -> resourceKeysResult
                    .map(resourceKeys -> resultsToMessages(results, resourceKeys, projectPath, contextInfo.stripTraces())))
                .map(KeyedMessagesBuilder::build))
            .ifElse(builder::addMessages, err -> builder.addMessages(err.toKeyedMessages()));

        return builder.build();
    }

    private KeyedMessagesBuilder getParseMessages(ExecContext context, HashSet<ResourceKey> resourceKeys) {
        KeyedMessagesBuilder builder = new KeyedMessagesBuilder();
        resourceKeys.forEach(resourceKey -> {
            try {
                Messages messages = context.require(parseMessageFunction.createSupplier(resourceKey));
                builder.addMessages(resourceKey, messages);
            } catch(UncheckedIOException e) {
                builder.addMessage("IO Exception when parsing file", e.getCause(), Severity.Error, resourceKey);
            }
        });
        return builder;
    }

    protected KeyedMessagesBuilder resultsToMessages(AnalysisResults results, Set<ResourceKey> fileKeys,
                                                     ResourceKey projectPath, boolean stripTraces) {
        final KeyedMessagesBuilder builder = new KeyedMessagesBuilder();
        final Result<IUniDisunifier, ?> resultUnifier = results.finalResult()
            .map(SolverResult::state)
            .map(IState.Immutable::unifier);

        // Add all file messages
        results.fileResults().entrySet().stream()
            .filter(entry -> entry.getKey().languageId().equals(getLanguageId()))
            .forEach(entry -> entry.getValue().ifElse(
                (FileResult fileResult) -> {
                    List<Message> resourceMessages = fileResult.result().messages().entrySet().stream()
                        .map(e -> MessageUtils.formatMessage(e.getValue(), e.getKey(), resultUnifier.getOr(fileResult.result().state().unifier()), !stripTraces))
                        .collect(Collectors.toList());
                    builder.addMessages(entry.getKey().resourceKey(), resourceMessages);
                },
                err -> builder.addMessages(err.toKeyedMessages())
            ));

        // Add project messages (if present)
        results.projectResults().computeIfPresent(getLanguageId(), (k, v) -> {
            v.ifElse(result -> {
                List<Message> resourceMessages = result.messages().entrySet().stream()
                    .map(e -> MessageUtils.formatMessage(e.getValue(), e.getKey(), resultUnifier.getOr(result.state().unifier()), !stripTraces))
                    .collect(Collectors.toList());
                builder.addMessages(projectPath, resourceMessages);
                },
                err -> builder.addMessages(err.toKeyedMessages())
            );
            return v;
        });

        // Process final result messages
        results.finalResult().ifElse(
            result -> result.messages().entrySet().stream()
                .map(e -> new AbstractMap.SimpleImmutableEntry<>(
                    defaultIfNull(MessageUtils.tryGetResourceKey(e.getKey(), result.state().unifier()), projectPath),
                    MessageUtils.formatMessage(e.getValue(), e.getKey(), result.state().unifier(), !stripTraces)))
                // For messages in the final result, it is not easily possible to determine which language they belong to
                // Therefore we include only messages on files which belong to this language
                // Messages without an origin are included on the project by default
                // These might be duplicated among languages
                .filter(message -> projectPath.equals(message.getKey()) || fileKeys.contains(message.getKey()))
                .forEach(entry -> builder.addMessage(entry.getValue(), entry.getKey())),
            err -> builder.addMessages(err.toKeyedMessages())
        );

        // Add empty message sets for project, to ensure old messages on project are cleared
        builder.addMessages(projectPath, Collections.emptySet());
        return builder;
    }

    protected abstract LanguageId getLanguageId();

    private static <T> T defaultIfNull(@Nullable T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
