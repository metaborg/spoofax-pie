package mb.statix.multilang;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.metaborg.util.log.Level;

import java.util.Set;
import java.util.stream.Stream;

@Value.Immutable
public interface ContextConfig {
    Set<LanguageId> languages();
    // String to enable case-insensitive parsing
    @Nullable String logLevel();

    @Value.Lazy default @Nullable Level parseLevel() {
        return Stream.of(Level.values())
            .filter(level -> level.toString().equalsIgnoreCase(logLevel()))
            .findFirst()
            .orElse(null);
    }
}
