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
import org.metaborg.util.iterators.Iterables2;

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
            this.projectPath = projectPath;
            this.analysisContext = analysisContext;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return projectPath.equals(input.projectPath) &&
                analysisContext.equals(input.analysisContext);
        }

        @Override public int hashCode() {
            return Objects.hash(projectPath, analysisContext);
        }

        @Override public String toString() {
            return "Input{" +
                "projectPath=" + projectPath +
                ", analysisContext=" + analysisContext +
                '}';
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
            if(!resourceMessages.isEmpty()) {
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

        // Add empty message sets for keys with no message, to ensure old messages on file are cleared
        results.fileResults().keySet()
            .stream()
            .map(AnalysisResults.FileKey::getResource)
            .forEach(key -> builder.addMessages(key, Iterables2.empty()));

        return builder.build();
    }
}
