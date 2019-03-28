package mb.jsglr1.common;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class JSGLR1ParseTable implements Serializable {
    final ParseTable internalParseTable;

    private JSGLR1ParseTable(ParseTable parseTable) {
        this.internalParseTable = parseTable;
    }

    public static JSGLR1ParseTable fromStream(InputStream parseTableStream) throws JSGLR1ParseTableException {
        final ITermFactory termFactory = new ImploderOriginTermFactory(new TermFactory());
        final TermReader reader = new TermReader(termFactory);
        try {
            final IStrategoTerm parseTableTerm = reader.parseFromStream(parseTableStream);
            final ParseTable parseTable = new ParseTable(parseTableTerm, termFactory);
            return new JSGLR1ParseTable(parseTable);
        } catch(IOException | InvalidParseTableException e) {
            throw new JSGLR1ParseTableException("Loading parse table from stream failed unexpectedly", e);
        }
    }

    public static JSGLR1ParseTable fromParseTable(ParseTable parseTable) {
        return new JSGLR1ParseTable(parseTable);
    }
}
