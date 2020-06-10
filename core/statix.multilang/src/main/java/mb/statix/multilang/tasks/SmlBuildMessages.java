package mb.statix.multilang.tasks;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Message;
import mb.nabl2.terms.unification.ud.IUniDisunifier;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.AnalysisContext;
import mb.statix.multilang.AnalysisResults;
import mb.statix.multilang.utils.MessageUtils;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SmlBuildMessages implements TaskDef<SmlBuildMessages.Input, KeyedMessages> {

    public static class Input implements Serializable {
        private final ResourcePath projectPath;
        private final AnalysisContext analysisContext;

        public Input(ResourcePath projectPath, AnalysisContext analysisContext) {
            this.projectPath = Objects.requireNonNull(projectPath, "SmlBuildMessages.Input.projectPath may not be null");;
            this.analysisContext = Objects.requireNonNull(analysisContext, "SmlBuildMessages.Input.analysisContext may not be null");;
        }
    }

    private final SmlAnalyzeProject analyzeProject;

    @Inject public SmlBuildMessages(SmlAnalyzeProject analyzeProject) {
        this.analyzeProject = analyzeProject;
    }

    @Override public String getId() {
        return SmlBuildMessages.class.getSimpleName();
    }

    @Override public KeyedMessages exec(ExecContext context, Input input) throws Exception {
        AnalysisResults results = context.require(analyzeProject.createTask(
            new SmlAnalyzeProject.Input(input.projectPath, input.analysisContext)
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
            .map(e -> new AbstractMap.SimpleEntry<>(MessageUtils.tryGetResourceKey(e.getKey(), results.finalResult().state().unifier()),
                MessageUtils.formatMessage(e.getValue(), e.getKey(), resultUnifier)))
            .collect(Collectors.groupingBy(e -> Optional.ofNullable(e.getKey())))
            .forEach((resourceKey, messages) -> builder.addMessages(resourceKey.orElse(null), messages.stream()
                .map(Map.Entry::getValue).collect(Collectors.toList())));

        return builder.build();
    }
}
