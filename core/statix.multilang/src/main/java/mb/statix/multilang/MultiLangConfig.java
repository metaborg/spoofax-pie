package mb.statix.multilang;

import org.immutables.value.Value;

import java.util.Map;

// Not immutable since it must have java beans semantics to work with yaml reader (see SmlReadConfigYaml).
@Value.Immutable
public interface MultiLangConfig {
    @Value.Parameter Map<LanguageId, ContextId> languageContexts();
    @Value.Parameter Map<ContextId, String> logging();
}
