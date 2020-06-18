package mb.statix.multilang;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MultiLangConfig {
    private Map<String, String> languageContexts = new HashMap<>();
    private Map<String, ContextConfig> customContexts = new HashMap<>();

    public Map<String, String> getLanguageContexts() {
        return languageContexts;
    }

    public void setLanguageContexts(Map<String, String> languageContexts) {
        this.languageContexts = languageContexts;
    }

    public Map<String, ContextConfig> getCustomContexts() {
        return customContexts;
    }

    public void setCustomContexts(Map<String, ContextConfig> customContexts) {
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
