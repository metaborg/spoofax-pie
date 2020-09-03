package mb.statix.multilang.pie.config;

import mb.statix.multilang.metadata.ContextId;
import mb.statix.multilang.metadata.LanguageId;
import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public interface MultiLangConfig {
    @Value.Parameter Map<LanguageId, ContextId> languageContexts();

    @Value.Parameter Map<ContextId, String> logging();
}
