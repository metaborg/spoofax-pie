package mb.statix.referenceretention.statix;

import com.google.common.collect.ImmutableList;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.build.AbstractApplTerm;
import mb.statix.referenceretention.statix.RRLockedReference;
import mb.statix.scopegraph.Scope;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import java.util.List;

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

    /**
     * Gets the term that represents the locked reference.
     * @return a term
     */
    @Value.Parameter public abstract ITerm getTerm();

    /**
     * Gets the declaration to which this locked reference resolved.
     * @return a declaration, represented by the declaration's unique scope
     */
    @Value.Parameter public abstract Scope getDeclaration();


    @Value.Lazy @Override public List<ITerm> getArgs() {
        return ImmutableList.of(getTerm(), getDeclaration());
    }

    @Override
    public String getOp() {
        return "_RRLockedReference";
    }

    @Override protected ARRLockedReference check() {
        return this;
    }

    @Override public String toString() {
        return "\uD83D\uDD12" + super.toString();
    }

}

