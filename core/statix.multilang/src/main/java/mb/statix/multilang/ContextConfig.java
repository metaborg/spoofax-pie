package mb.statix.multilang;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ContextConfig {
    private List<LanguageId> languages;
    // String to enable case-insensitive parsing
    private @Nullable String logLevel;

    public List<LanguageId> getLanguages() {
        return languages;
    }

    public void setLanguages(List<LanguageId> languages) {
        this.languages = languages;
    }

    public @Nullable String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(@Nullable String log) {
        this.logLevel = log;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ContextConfig that = (ContextConfig)o;
        return Objects.equals(languages, that.languages) &&
            Objects.equals(logLevel, that.logLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(languages, logLevel);
    }

    @Override public String toString() {
        return "ContextConfig{" +
            "languages=" + languages +
            ", logLevel='" + logLevel + '\'' +
            '}';
    }

    public @Nullable Level parseLevel() {
        return Arrays.stream(Level.values())
            .filter(e -> e.name().equalsIgnoreCase(logLevel))
            .findAny()
            .orElse(null);
    }
}
