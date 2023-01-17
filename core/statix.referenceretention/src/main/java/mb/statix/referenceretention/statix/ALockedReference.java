package mb.statix.referenceretention.statix;

import com.google.common.collect.ImmutableList;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.build.AbstractApplTerm;
import mb.statix.scopegraph.Scope;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import java.util.List;

import static mb.nabl2.terms.build.TermBuild.B;
import static mb.nabl2.terms.matching.TermMatch.M;

/**
 * A locked reference is a reference for which the declaration it refers to is known.
 */
@Value.Immutable(lazyhash = false)
@Serial.Version(value = 42L)
@Deprecated // Not used, use Stratego version?
public abstract class ALockedReference extends AbstractApplTerm {


    /**
     * Gets the term that represents the locked reference.
     *
     * @return a term
     */
    @Value.Parameter public abstract ITerm getTerm();

    /**
     * Gets the declaration to which this locked reference resolved.
     *
     * @return a declaration, represented by the declaration's unique scope
     */
    @Value.Parameter public abstract Scope getDeclaration();


    @Value.Lazy @Override public List<ITerm> getArgs() {
        return ImmutableList.of(getTerm(), getDeclaration());
    }

    @Override
    public String getOp() {
        return "_LockedReference";
    }


    @Override protected ALockedReference check() {
        return this;
    }

    @Override public String toString() {
        return "\uD83D\uDD12" + super.toString();
    }

}

