package mb.spoofax.compiler.interfaces.spoofaxcore;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.function.Function;

public interface CodeCompleterFactory {
    CodeCompleter create(
        Function<IStrategoTerm, @Nullable IStrategoTerm> explicate,
        Function<IStrategoTerm, @Nullable IStrategoTerm> implicate,
        Function<IStrategoTerm, @Nullable IStrategoTerm> upgrade,
        Function<IStrategoTerm, @Nullable IStrategoTerm> downgrade,
        Function<IStrategoTerm, @Nullable IStrategoTerm> isInjection,
        Function<IStrategoTerm, @Nullable String> prettyPrint
    );
}
