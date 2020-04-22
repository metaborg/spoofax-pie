package mb.spoofax.compiler.util;

import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Optional;

@Value.Immutable
public abstract class Coordinate implements Serializable {
    public static Coordinate of(String groupId, String artifactId, String version) {
        return ImmutableCoordinate.of(groupId, artifactId, Optional.of(version));
    }

    public static Coordinate of(String groupId, String artifactId) {
        return ImmutableCoordinate.of(groupId, artifactId, Optional.empty());
    }

    public static Coordinate fromGradleNotation(String notation) {
        final int firstIdx = notation.indexOf(':');
        if(firstIdx == -1) {
            throw new IllegalArgumentException("Input '" + notation + "' is not a valid Gradle dependency notation with 3 elements");
        }
        final String groupId = notation.substring(0, firstIdx);

        final int lastIdx = notation.lastIndexOf(':');
        if(lastIdx != -1 && lastIdx != firstIdx) {
            final String artifactId = notation.substring(firstIdx + 1, lastIdx);
            final String version = notation.substring(lastIdx + 1);
            return Coordinate.of(groupId, artifactId, version);
        } else {
            final String artifactId = notation.substring(firstIdx + 1);
            return Coordinate.of(groupId, artifactId);
        }
    }


    public static class Builder extends ImmutableCoordinate.Builder {}

    public static Builder builder() {
        return new Builder();
    }


    @Value.Parameter public abstract String groupId();

    @Value.Parameter public abstract String artifactId();

    @Value.Parameter public abstract Optional<String> version();


    @Value.Derived public String toGradleNotation() {
        return groupId() + ":" + artifactId() + version().map(v -> ":" + v).orElse("");
    }


    @Override
    public String toString() {
        return toGradleNotation();
    }
}
