package mb.statix.referenceretention.statix;

import com.google.common.collect.ImmutableList;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.build.AbstractApplTerm;
import mb.nabl2.terms.matching.TermMatch;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.nabl2.terms.matching.TermMatch.M;

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

    private static final String OP = "_RRPlaceholder";

    /**
     * Gets the body term.
     * @return a term, which may include {@link RRLockedReference} terms
     */
    @Value.Parameter public abstract ITerm getBody();

    /**
     * Gets the context terms for the references inside the placeholder's AST.
     * @return the contexts, which are terms that describe the context references,
     * such as {@code Var("x")} or {@code Member("x", Var("y"))}
     */
    @Value.Parameter public abstract List<ITerm> getContextTerms();

    @Override public String getOp() {
        return OP;
    }

    @Value.Lazy @Override public List<ITerm> getArgs() {
        return ImmutableList.of(getBody(), B.newList(getContextTerms()));
    }

    public static TermMatch.IMatcher<RRPlaceholder> matcher() {
        return M.preserveAttachments(M.appl2(OP, M.term(), M.listElems(), (t, body, contexts) -> {
            if(t instanceof RRPlaceholder) {
                return (RRPlaceholder) t;
            } else {
                return RRPlaceholder.of(body, contexts);
            }
        }));
    }

    @Override protected ARRPlaceholder check() {
        return this;
    }

    @Override public int hashCode() {
        // We use the super-class hashcode to ensure that an ARRPlaceholder and an IApplTerm
        // with the same term representation have the same hash code.
        // Super-class caches hashcode.
        return super.hashCode();
    }

    @Override public boolean equals(Object other) {
        if(this == other) return true;
        if(!(other instanceof RRPlaceholder)) return super.equals(other);
        final RRPlaceholder that = (RRPlaceholder)other;
        if(this.hashCode() != that.hashCode()) return false;
        // @formatter:off
        return Objects.equals(this.getBody(), that.getBody())
            && Objects.equals(this.getContextTerms(), that.getContextTerms());
        // @formatter:on
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[[");
        sb.append(getBody());
        sb.append("|");
        if (getContextTerms() != null && !getContextTerms().isEmpty()) {
            final Iterator<ITerm> it = getContextTerms().iterator();
            sb.append(it.next());
            while(it.hasNext()) {
                sb.append(", ");
                sb.append(it.next());
            }
        } else {
            sb.append("ε");
        }
        sb.append("]]");
        return sb.toString();
    }

}

