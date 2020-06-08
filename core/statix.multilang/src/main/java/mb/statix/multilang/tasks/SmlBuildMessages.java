package mb.statix.multilang.tasks;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.statix.constraints.messages.IMessage;
import mb.statix.multilang.AnalysisContext;
import mb.statix.multilang.AnalysisResults;
import mb.statix.multilang.utils.MessageUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SmlBuildMessages implements TaskDef<SmlBuildMessages.Input, KeyedMessages> {

    public static class Input implements Serializable {
        private final AnalysisContext analysisContext;

        public Input(AnalysisContext analysisContext) {
            this.analysisContext = analysisContext;
        }
    }

    private final SmlAnalyzeProject analyzeProject;

    public SmlBuildMessages(SmlAnalyzeProject analyzeProject) {
        this.analyzeProject = analyzeProject;
    }

    @Override public String getId() {
        return SmlBuildMessages.class.getSimpleName();
    }

    @Override public KeyedMessages exec(ExecContext context, Input input) throws Exception {
        AnalysisResults results = context.require(analyzeProject.createTask(
            new SmlAnalyzeProject.Input(input.analysisContext)
        )).getResults();

        final IUniDisunifier resultUnifier = results.finalResult().state().unifier();
        KeyedMessagesBuilder builder = new KeyedMessagesBuilder();

        // Add all file messages
        results.fileResults().forEach((key, fileResult) -> {
            List<Message> resourceMessages = fileResult.getResult().messages().entrySet().stream()
                .map(e -> MessageUtils.formatMessage(e.getValue(), e.getKey(), resultUnifier))
                .collect(Collectors.toList());
            if (!resourceMessages.isEmpty()) {
                builder.addMessages(key.getResource(), resourceMessages);
            }
        });

        // Process final result messages
        results.finalResult().messages().entrySet().stream()
            .map(e -> new AbstractMap.SimpleEntry<>(tryGetResourceKey(e.getValue()),
                MessageUtils.formatMessage(e.getValue(), e.getKey(), resultUnifier)))
            .collect(Collectors.groupingBy(e -> Optional.ofNullable(e.getKey())))
            .forEach((resourceKey, messages) -> builder.addMessages(resourceKey.orElse(null), messages.stream()
                .map(Map.Entry::getValue).collect(Collectors.toList())));

        return builder.build();
    }

    private @Nullable ResourceKey tryGetResourceKey(IMessage message) {
        return message.origin().map(MessageUtils::resourceKeyFromOrigin).orElse(null);
    }
}
