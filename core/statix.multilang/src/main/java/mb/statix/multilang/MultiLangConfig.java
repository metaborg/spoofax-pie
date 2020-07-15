package mb.statix.multilang;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Not immutable since it must have java beans semantics to work with yaml reader (see SmlReadConfigYaml).
public class MultiLangConfig implements Serializable {
    private HashMap<LanguageId, ContextId> languageContexts;
    private HashMap<ContextId, String> logging;

    public MultiLangConfig(HashMap<LanguageId, ContextId> languageContexts, HashMap<ContextId, String> logging) {
        this.languageContexts = languageContexts;
        this.logging = logging;
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

    public Map<ContextId, String> getLogging() {
        return Collections.unmodifiableMap(logging);
    }

    public void setLogging(HashMap<ContextId, String> logging) {
        this.logging = logging;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        MultiLangConfig that = (MultiLangConfig)o;
        return Objects.equals(languageContexts, that.languageContexts) &&
            Objects.equals(logging, that.logging);
    }

    @Override
    public int hashCode() {
        return Objects.hash(languageContexts, logging);
    }

    @Override public String toString() {
        return "MultiLangConfig{" +
            "languageContexts=" + languageContexts +
            ", logging=" + logging +
            '}';
    }
}
