package mb.sdf3.stratego;

import java.io.Serializable;
import java.util.Objects;

/**
 * SDF3 context.
 *
 * Add this context to a Stratego context using {@link mb.stratego.common.StrategoRuntime#addContextObject}.
 */
public final class Sdf3Context implements Serializable {
    /** The language specification name as a Stratego qualifier. */
    public final String strategoQualifier;
    /** The prefix for placeholders; or {@code null} to use the default. */
    public final String placeholderPrefix;
    /** The suffix for placeholders; or {@code null} to use the default. */
    public final String placeholderSuffix;

    public Sdf3Context(
        String strategoQualifier,
        String placeholderPrefix,
        String placeholderSuffix
    ) {
        this.strategoQualifier = strategoQualifier;
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Sdf3Context that = (Sdf3Context)o;
        return strategoQualifier.equals(that.strategoQualifier)
            && placeholderPrefix.equals(that.placeholderPrefix)
            && placeholderSuffix.equals(that.placeholderSuffix);
    }

    @Override public int hashCode() {
        return Objects.hash(
            strategoQualifier,
            placeholderPrefix,
            placeholderSuffix
        );
    }

    @Override public String toString() {
        return "Sdf3Context{" +
            "strategoQualifier='" + strategoQualifier + '\'' +
            ", placeholderPrefix='" + placeholderPrefix + '\'' +
            ", placeholderSuffix='" + placeholderSuffix + '\'' +
            '}';
    }
}
