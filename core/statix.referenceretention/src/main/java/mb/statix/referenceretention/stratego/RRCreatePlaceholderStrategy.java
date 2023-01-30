package mb.statix.referenceretention.stratego;

import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ListTerms;
import mb.nabl2.terms.Terms;
//import mb.nabl2.terms.build.TermBuild;
import static mb.nabl2.terms.build.TermBuild.B;
//import mb.nabl2.terms.matching.TermMatch;
import static mb.nabl2.terms.matching.TermMatch.M;
import mb.statix.referenceretention.statix.RRPlaceholder;
import mb.statix.spoofax.StatixPrimitive;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.unit.Unit;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Creates a reference retention placeholder, that is, a placeholder of the form {@code [[ <body> | <contexts> ]]}
 * where {@code <body>} is the syntactic term wrapped by the placeholder and {@code <contexts>} is the context
 * in which the references in the body must be resolved.
 * <p>
 * For example, when inlining a method body, the body is wrapped in a placeholder and the context is the instance
 * on which the method was called. The {@link RRFixReferencesStrategy} will gradually unwrap the placeholder's body
 * and fix any references it finds using the given context.
 * <p>
 * To call this strategy, use {@code prim("RR_create_placeholder", ctxs)}, where the current term becomes the placeholder
 * body and the {@code ctx} becomes the placeholder context.
 */
public final class RRCreatePlaceholderStrategy extends StatixPrimitive {
    public static final String NAME = "RR_create_placeholder";
    public RRCreatePlaceholderStrategy() {
        super(NAME, 1);
    }

    @Override
    protected Optional<? extends ITerm> call(IContext env, ITerm body, List<ITerm> terms) throws InterpreterException {
//        final ArrayList<ITerm> contexts = new ArrayList<>();
//        TermMatch.M.list().match(terms.get(0)).map(l -> l.match(ListTerms.cases(
//            (cons) -> {
//                contexts.add(cons);
//                return Unit.unit;
//            },
//            (nil) -> Unit.unit,
//            (var) -> Unit.unit
//        )));
        @Nullable final ITerm result = eval(
            body,
            // Either get a list of context terms, or a single context term in a singleton list
            M.list().match(terms.get(0)).orElse(B.newList(terms.get(0)))
        );
        return Optional.ofNullable(result);
    }

    // Usage:
    // <prim("RR_create_placeholder", ctxs)> body
    @Nullable private ITerm eval(ITerm body, IListTerm contexts) throws InterpreterException {
        return RRPlaceholder.of(body, contexts);
    }

}

