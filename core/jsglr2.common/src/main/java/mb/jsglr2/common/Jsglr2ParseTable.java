package mb.jsglr2.common;

import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.ParseTableReadException;
import org.metaborg.parsetable.ParseTableReader;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

import java.io.IOException;
import java.io.InputStream;

public class Jsglr2ParseTable {
    final IParseTable parseTable;

    private Jsglr2ParseTable(IParseTable parseTable) {
        this.parseTable = parseTable;
    }

    public static Jsglr2ParseTable fromStream(InputStream parseTableStream) throws Jsglr2ParseTableException {
        final ITermFactory termFactory = new ImploderOriginTermFactory(new TermFactory());
        final TermReader reader = new TermReader(termFactory);
        try {
            final IStrategoTerm parseTableTerm = reader.parseFromStream(parseTableStream);
            final IParseTable parseTable = new ParseTableReader().read(parseTableTerm);
            return new Jsglr2ParseTable(parseTable);
        } catch(IOException | ParseTableReadException e) {
            throw new Jsglr2ParseTableException("Loading parse table from stream failed unexpectedly", e);
        }
    }

    public static Jsglr2ParseTable fromParseTable(IParseTable parseTable) {
        return new Jsglr2ParseTable(parseTable);
    }
}
