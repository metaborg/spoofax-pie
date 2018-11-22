package mb.spoofax.legacy;

import mb.fs.api.node.FSNode;
import org.spoofax.interpreter.terms.*;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.io.binary.TermReader;

import javax.annotation.Nullable;
import java.io.IOException;

public class EsvUtil {
    public static @Nullable IStrategoAppl read(FSNode file) throws ParseError, IOException {
        final ITermFactory termFactory =
            StaticSpoofaxCoreFacade.spoofax().termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE);
        final TermReader reader = new TermReader(termFactory);
        final IStrategoTerm term = reader.parseFromStream(file.newInputStream());
        if(term.getTermType() != IStrategoTerm.APPL) {
            return null;
        }
        return (IStrategoAppl) term;
    }
}
