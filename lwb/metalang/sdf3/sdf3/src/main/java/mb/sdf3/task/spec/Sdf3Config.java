package mb.sdf3.task.spec;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * SDF3 configuration parameters.
 */
public final class Sdf3Config implements Serializable {
    public final String placeholderPrefix;
    public final String placeholderSuffix;

    /**
     * Initializes a new instance of the {@link Sdf3Config} class.
     *
     * @param placeholderPrefix the placeholder prefix
     * @param placeholderSuffix the placeholder suffix
     */
    public Sdf3Config(
        String placeholderPrefix,
        String placeholderSuffix
    ) {
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
    }

    public static Sdf3Config createDefault() {
        return new Sdf3Config("$", "");
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Sdf3Config that = (Sdf3Config)o;
        return placeholderPrefix.equals(that.placeholderPrefix)
            && placeholderSuffix.equals(that.placeholderSuffix);
    }

    @Override public int hashCode() {
        return Objects.hash(
            placeholderPrefix,
            placeholderSuffix
        );
    }

    @Override public String toString() {
        return "Sdf3Config{" +
            "placeholderPrefix='" + placeholderPrefix + '\'' +
            ", placeholderSuffix='" + placeholderSuffix + '\'' +
            '}';
    }
}
