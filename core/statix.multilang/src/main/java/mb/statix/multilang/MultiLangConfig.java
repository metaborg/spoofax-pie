package mb.statix.multilang;

import mb.common.util.MapView;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Not immutable since it must have java beans semantics to work with yaml reader (see SmlReadConfigYaml).
public class MultiLangConfig implements Serializable {
    private HashMap<LanguageId, ContextId> languageContexts = new HashMap<>();
    private HashMap<ContextId, ContextConfig> customContexts = new HashMap<>();

    public MultiLangConfig(HashMap<LanguageId, ContextId> languageContexts, HashMap<ContextId, ContextConfig> customContexts) {
        this.languageContexts = languageContexts;
        this.customContexts = customContexts;
    }

    public MultiLangConfig() {
        this(new HashMap<>(), new HashMap<>());
    }

    public Map<LanguageId, ContextId> getLanguageContexts() {
        return Collections.unmodifiableMap(languageContexts);
    }

    public void setLanguageContexts(HashMap<LanguageId, ContextId> languageContexts) {
        this.languageContexts = languageContexts;
    }

    public Map<ContextId, ContextConfig> getCustomContexts() {
        return Collections.unmodifiableMap(customContexts);
    }

    public void setCustomContexts(HashMap<ContextId, ContextConfig> customContexts) {
        this.customContexts = customContexts;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        MultiLangConfig that = (MultiLangConfig)o;
        return Objects.equals(languageContexts, that.languageContexts) &&
            Objects.equals(customContexts, that.customContexts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(languageContexts, customContexts);
    }

    @Override public String toString() {
        return "MultiLangConfig{" +
            "languageContexts=" + languageContexts +
            ", customContexts=" + customContexts +
            '}';
    }
}
