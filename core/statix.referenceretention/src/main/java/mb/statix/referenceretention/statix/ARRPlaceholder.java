package mb.statix.referenceretention.statix;

import com.google.common.collect.ImmutableList;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.build.AbstractApplTerm;
import mb.statix.referenceretention.statix.RRLockedReference;
import mb.statix.referenceretention.statix.RRPlaceholder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import java.util.List;

/**
 * A reference retention placeholder is a term that wraps a body and a context in which references in the body must be
 * resolved. The body is a term representing syntax in the object language, and can contain references protected by
 * {@link RRLockedReference} terms. The context is a reference that is valid in the scope in which the placeholder is
 * inserted.
 * <p>
 * This class cannot be instantiated, but its derived class {@link RRPlaceholder} can.
 */
@Value.Immutable(lazyhash = false)
@Serial.Version(value = 42L)
@Deprecated // Not used, use Stratego version?
public abstract class ARRPlaceholder extends AbstractApplTerm {

    /**
     * Gets the body term.
     * @return a term, which may include {@link RRLockedReference} terms
     */
    @Value.Parameter public abstract ITerm getBody();

    /**
     * Gets the context for the references inside the placeholder's AST.
     * @return the context, which is a term that describes the context reference,
     * such as {@code Var("x")} or {@code Member("x", Var("y"))}; or {@code null}
     */
    @Value.Parameter @Nullable public abstract ITerm getContext();


    @Value.Lazy @Override public List<ITerm> getArgs() {
        if (getContext() == null)
            return ImmutableList.of(getBody());
        else
            return ImmutableList.of(getBody(), getContext());
    }

    @Override
    public String getOp() {
        return "_RRPlaceholder";
    }

    @Override protected ARRPlaceholder check() {
        return this;
    }

    @Override public String toString() {
        return "[[" + getBody() + "|" + (getContext() != null ? getContext() : "") + "]]";
    }

}

