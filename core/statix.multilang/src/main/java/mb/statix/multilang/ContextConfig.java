package mb.statix.multilang;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ContextConfig  implements Serializable {
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
        return Stream.of(Level.values())
            .filter(level -> level.toString().equalsIgnoreCase(this.logLevel))
            .findFirst()
            .orElse(null);
    }
}
