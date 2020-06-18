package mb.statix.multilang;

import mb.log.api.Logger;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.resource.ResourceRegistry;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.Level;
import org.metaborg.util.log.LoggerUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class AnalysisContext implements Serializable {

    @Value.Parameter public abstract ContextId contextId();

    @Value.Parameter public abstract Map<LanguageId, LanguageMetadata> languages();

    // Pie instance to derive Pie instance for use in this context from
    @Value.Parameter public abstract Pie basePie();

    @Value.Parameter public abstract ResourceService baseResourceService();

    @Value.Parameter @Value.Auxiliary
    public abstract Logger logger();

    @Value.Default public @Nullable Level stxLogLevel() {
        return Level.Warn;
    }

    @Value.Lazy public Pie createPieForContext() {
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

    // Statix debug contexts still use old logger API
    // TODO: update statix logging / write adapter class
    @Value.Lazy public ILogger stxLogger() {
        return LoggerUtils.logger(String.format("SLA [%s]", contextId()));
    }

    @Override
    public String toString() {
        return "AnalysisContext{contextId=" + contextId() + '}';
    }
}
