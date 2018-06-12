package mb.spoofax.legacy;

import java.io.IOException;
import javax.annotation.Nullable;
import mb.pie.vfs.path.PPath;
import org.spoofax.interpreter.terms.*;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.io.binary.TermReader;

public class EsvUtil {
    public static @Nullable IStrategoAppl read(PPath file) throws ParseError, IOException {
        final ITermFactory termFactory =
            StaticSpoofaxCoreFacade.spoofax().termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE);
        final TermReader reader = new TermReader(termFactory);
        final IStrategoTerm term = reader.parseFromStream(file.inputStream());
        if(term.getTermType() != IStrategoTerm.APPL) {
            return null;
        }
        return (IStrategoAppl) term;
    }
}
