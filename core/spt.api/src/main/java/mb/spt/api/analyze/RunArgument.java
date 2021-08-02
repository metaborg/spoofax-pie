package mb.spt.api.analyze;

import mb.common.region.Region;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;

import java.io.Serializable;
import java.util.Optional;

@ADT
public abstract class RunArgument implements Serializable {
    public interface Cases<R> {
        R intArg(IStrategoInt number);
        R stringArg(IStrategoString string);
        R selectionArg(Region region);
    }

    public abstract <R> R match(Cases<R> cases);

    public static RunArgument intArg(IStrategoInt value) {
        return RunArguments.intArg(value);
    }

    public static RunArgument stringArg(IStrategoString value) {
        return RunArguments.stringArg(value);
    }

    public static RunArgument selectionArg(Region value) {
        return RunArguments.selectionArg(value);
    }

    public RunArguments.CaseOfMatchers.TotalMatcher_IntArg caseOf() {
        return RunArguments.caseOf(this);
    }

    public Optional<IStrategoInt> getNumber() {
        return RunArguments.getNumber(this);
    }

    public Optional<IStrategoString> getString() {
        return RunArguments.getString(this);
    }

    public Optional<Region> getRegion() {
        return RunArguments.getRegion(this);
    }

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
