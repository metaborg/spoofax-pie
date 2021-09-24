package mb.spoofax.compiler.interfaces.spoofaxcore;

import mb.common.result.Result;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.function.Function;

public interface CodeCompleterFactory {
    CodeCompleter create(
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> explicate,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> implicate,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> upgrade,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> downgrade,
        Function<IStrategoTerm, Result<IStrategoTerm, ?>> isInjection,
        Function<IStrategoTerm, Result<String, ?>> prettyPrint
    );
}
