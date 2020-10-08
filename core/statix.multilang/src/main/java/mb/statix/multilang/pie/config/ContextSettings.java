package mb.statix.multilang.pie.config;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface ContextSettings {
    @Value.Default default boolean stripTraces() {
        return false;
    }

    Optional<String> logLevel();
}
