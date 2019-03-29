package mb.spoofax.core.language;

import mb.fs.api.path.FSPath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface AstService {
    @Nullable IStrategoTerm getAst(FSPath path);
}
