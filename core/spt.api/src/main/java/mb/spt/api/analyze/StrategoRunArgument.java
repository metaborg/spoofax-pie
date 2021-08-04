package mb.spt.api.analyze;

import mb.common.region.Region;
import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoString;

import java.io.Serializable;
import java.util.Optional;

@ADT
public abstract class StrategoRunArgument implements Serializable {
    public interface Cases<R> {
        R intArg(IStrategoInt number);
        R stringArg(IStrategoString string);
        R selectionArg(Region region);
    }

    public abstract <R> R match(Cases<R> cases);

    public static StrategoRunArgument intArg(IStrategoInt value) {
        return StrategoRunArguments.intArg(value);
    }

    public static StrategoRunArgument stringArg(IStrategoString value) {
        return StrategoRunArguments.stringArg(value);
    }

    public static StrategoRunArgument selectionArg(Region value) {
        return StrategoRunArguments.selectionArg(value);
    }

    public StrategoRunArguments.CaseOfMatchers.TotalMatcher_IntArg caseOf() {
        return StrategoRunArguments.caseOf(this);
    }

    public Optional<IStrategoInt> getNumber() {
        return StrategoRunArguments.getNumber(this);
    }

    public Optional<IStrategoString> getString() {
        return StrategoRunArguments.getString(this);
    }

    public Optional<Region> getRegion() {
        return StrategoRunArguments.getRegion(this);
    }

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
