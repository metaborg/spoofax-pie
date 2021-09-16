package mb.statix.strategies.runtime;

import mb.statix.sequences.Seq;
import mb.statix.sequences.SeqBase;
import mb.statix.strategies.NamedStrategy;
import mb.statix.strategies.NamedStrategy2;
import mb.statix.strategies.Strategy;
import mb.statix.utils.ExcludeFromJacocoGeneratedReport;

/**
 * Boolean 'not' strategy.
 *
 * @param <CTX> the type of context (invariant)
 */
public final class NotStrategy<CTX> extends NamedStrategy<CTX, Boolean, Boolean> {

    @SuppressWarnings({"rawtypes", "RedundantSuppression"})
    private static final NotStrategy instance = new NotStrategy();
    @SuppressWarnings({"unchecked", "unused", "RedundantCast", "RedundantSuppression"})
    public static <CTX> NotStrategy<CTX> getInstance() { return (NotStrategy<CTX>)instance; }

    private NotStrategy() { /* Prevent instantiation. Use getInstance(). */ }

    public static <CTX> Seq<Boolean> eval(TegoEngine engine, CTX ctx, Boolean input) {
        return Seq.of(!input);
    }

    @Override
    public Seq<Boolean> evalInternal(TegoEngine engine, CTX ctx, Boolean input) {
        return eval(engine, ctx, input);
    }

    @Override
    public String getName() {
        return "not";
    }
}
