package mb.statix.referenceretention.statix;

import com.google.common.collect.ImmutableList;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.build.AbstractApplTerm;
import mb.nabl2.terms.matching.TermMatch;
import mb.nabl2.terms.stratego.TermIndex;
import mb.statix.referenceretention.statix.RRLockedReference;
import mb.statix.scopegraph.Scope;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static mb.nabl2.terms.matching.TermMatch.M;

/**
 * A locked reference is a reference for which the declaration it refers to is known
 * and should not change.
 * <p>
 * A reference is resolved to a specific declaration scope using Statix. After this resolution, moving the reference
 * (e.g., when inlining a method body), this could invalidate the reference. To prevent this, the reference syntax and
 * the declaration scope it resolved to are wrapped in this term.
 * <p>
 * This class cannot be instantiated, but its derived class {@link RRLockedReference} can.
 */
@Value.Immutable(lazyhash = false)
@Serial.Version(value = 42L)
public abstract class ARRLockedReference extends AbstractApplTerm {

    private static final String OP = "_RRLockedReference";

    /**
     * Gets the term that represents the locked reference.
     * @return a term
     */
    @Value.Parameter public abstract ITerm getTerm();

    /**
     * Gets the declaration to which this locked reference resolved.
     * @return a declaration, represented by the declaration's unique scope
     */
    @Value.Parameter public abstract TermIndex getDeclaration();

    @Override public String getOp() {
        return OP;
    }

    @Value.Lazy @Override public List<ITerm> getArgs() {
        return ImmutableList.of(getTerm(), getDeclaration());
    }

    public static TermMatch.IMatcher<RRLockedReference> matcher() {
        return M.preserveAttachments(M.appl2(OP, M.term(), TermIndex.matcher(), (t, term, decl) -> {
            if(t instanceof RRLockedReference) {
                return (RRLockedReference) t;
            } else {
                return RRLockedReference.of(term, decl);
            }
        }));
    }

    @Override protected ARRLockedReference check() {
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
        if(!(other instanceof RRLockedReference)) return super.equals(other);
        final RRLockedReference that = (RRLockedReference)other;
        if(this.hashCode() != that.hashCode()) return false;
        // @formatter:off
        return Objects.equals(this.getTerm(), that.getTerm())
            && Objects.equals(this.getDeclaration(), that.getDeclaration());
        // @formatter:on
    }

    @Override public String toString() {
        return "\uD83D\uDD12" + super.toString();
    }

}

