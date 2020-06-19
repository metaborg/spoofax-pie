package mb.statix.multilang;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MultiLangConfig {
    private Map<LanguageId, ContextId> languageContexts = new HashMap<>();
    private Map<ContextId, ContextConfig> customContexts = new HashMap<>();

    public Map<LanguageId, ContextId> getLanguageContexts() {
        return languageContexts;
    }

    public void setLanguageContexts(Map<LanguageId, ContextId> languageContexts) {
        this.languageContexts = languageContexts;
    }

    public Map<ContextId, ContextConfig> getCustomContexts() {
        return customContexts;
    }

    public void setCustomContexts(Map<ContextId, ContextConfig> customContexts) {
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
