package mb.spoofax.compiler.util;

import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public abstract class Coordinate implements Serializable {
    public static Coordinate of(String groupId, String artifactId, String version) {
        return ImmutableCoordinate.of(groupId, artifactId, version);
    }

    public static Coordinate fromGradleNotation(String notation) {
        final int firstIdx = notation.indexOf(':');
        if(firstIdx == -1) {
            throw new IllegalArgumentException("Input '" + notation + "' is not a valid Gradle dependency notation with 3 elements");
        }
        final int lastIdx = notation.lastIndexOf(':');
        if(lastIdx == -1 || lastIdx == firstIdx) {
            throw new IllegalArgumentException("Input '" + notation + "' is not a valid Gradle dependency notation with 3 elements");
        }
        final String groupId = notation.substring(0, firstIdx);
        final String artifactId = notation.substring(firstIdx + 1, lastIdx);
        final String version = notation.substring(lastIdx + 1);
        return Coordinate.of(groupId, artifactId, version);
    }


    public static class Builder extends ImmutableCoordinate.Builder {}

    public static Builder builder() {
        return new Builder();
    }


    @Value.Parameter public abstract String groupId();

    @Value.Parameter public abstract String artifactId();

    @Value.Parameter public abstract String version();


    @Value.Derived public String gradleNotation() {
        return groupId() + ":" + artifactId() + ":" + version();
    }


    @Override
    public String toString() {
        return gradleNotation();
    }
}
