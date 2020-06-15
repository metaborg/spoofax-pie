package mb.statix.multilang;

import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceService;
import org.immutables.value.Value;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Value.Immutable
public interface AnalysisContext extends Serializable {

    @Value.Parameter String contextId();

    @Value.Parameter Map<LanguageId, LanguageMetadata> languages();

    // Pie instance to derive Pie instance for use in this context from
    @Value.Parameter Pie basePie();

    @Value.Parameter ResourceService baseResourceService();

    @Value.Lazy default Pie createPieForContext() {
        Set<TaskDef<?, ?>> taskDefs = languages()
            .values()
            .stream()
            .map(LanguageMetadata::taskDefs)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        Set<ResourceRegistry> registries = languages()
            .values()
            .stream()
            .map(LanguageMetadata::resourceRegistries)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        return basePie()
            .createChildBuilder()
            .withTaskDefs(new MapTaskDefs(taskDefs))
            .withResourceService(baseResourceService().createChild(registries))
            .build();
    }

    @Value.Lazy default ILogger logger() {
        return LoggerUtils.logger(String.format("MLA [%s]", contextId()));
    }
}
