package mb.statix.referenceretention.statix;

import com.google.common.collect.ImmutableList;
import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ListTerms;
import mb.nabl2.terms.build.AbstractApplTerm;
import mb.statix.referenceretention.statix.RRLockedReference;
import mb.statix.referenceretention.statix.RRPlaceholder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.metaborg.util.unit.Unit;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
    @Value.Parameter public abstract IListTerm getContexts();


    @Value.Lazy @Override public List<ITerm> getArgs() {
        return ImmutableList.of(getBody(), getContexts());
    }

    @Override
    public String getOp() {
        return "_RRPlaceholder";
    }

    @Override protected ARRPlaceholder check() {
        return this;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[[");
        sb.append(getBody());
        sb.append("|");
        final AtomicBoolean first = new AtomicBoolean(true);
        getContexts().match(ListTerms.cases(
            (cons) -> {
                if (!first.getAndSet(false)) sb.append(", ");
                sb.append(cons);
                return Unit.unit;
            },
            (nil) -> {
                if (first.getAndSet(false)) sb.append("ε");
                return Unit.unit;
            },
            (var) -> {
                if (!first.getAndSet(false)) sb.append(", ");
                sb.append("<");
                sb.append(var);
                sb.append(">");
                return Unit.unit;
            }
        ));
        sb.append("]]");
        return sb.toString();
    }

}

