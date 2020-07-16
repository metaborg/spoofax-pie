package mb.statix.multilang;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public interface MultiLangConfig {
    @Value.Parameter Map<LanguageId, ContextId> languageContexts();

    @Value.Parameter Map<ContextId, String> logging();
}
