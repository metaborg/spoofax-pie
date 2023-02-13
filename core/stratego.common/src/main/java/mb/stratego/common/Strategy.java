package mb.stratego.common;

import mb.common.util.ADT;
import mb.common.util.ListView;
import mb.common.util.MapView;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.function.Function;
import java.util.function.Supplier;

@ADT
public abstract class Strategy {

    interface Cases<R> {
        R id();

        R fail();

        R invoke(String name);

        R strategy(String name, ListView<Strategy> strategyArguments, ListView<IStrategoTerm> termArguments);
    }

    public abstract <R> R match(Cases<R> cases);

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(Object obj);

    @Override public abstract String toString();

    public static Strategy id() {
        return Strategies.id();
    }

    public static Strategy fail() {
        return Strategies.fail();
    }

    public static Strategy strategy(String name, ListView<Strategy> strategyArguments, ListView<IStrategoTerm> termArguments) {
        return strategyArguments.isEmpty() && termArguments.isEmpty() ?
            Strategies.invoke(name) :
            Strategies.strategy(name, strategyArguments, termArguments);
    }

    public static Strategy strategy(String name) {
        return Strategies.invoke(name);
    }

    public <R> Cases<R> cases(Supplier<R> id, Supplier<R> fail, Function<String, R> invoke,
        Strategies.StrategyMapper<R> strategy) {
        return Strategies.cases(id, fail, invoke, strategy);
    }

    public Strategies.CasesMatchers.TotalMatcher_Id cases() {
        return Strategies.cases();
    }

}
