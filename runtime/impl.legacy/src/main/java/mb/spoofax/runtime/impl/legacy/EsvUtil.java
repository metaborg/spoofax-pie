package mb.spoofax.runtime.impl.legacy;

import java.io.IOException;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.io.binary.TermReader;

import mb.vfs.path.PPath;

public class EsvUtil {
    public static @Nullable IStrategoAppl read(PPath file) throws ParseError, IOException, MetaborgException {
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
