package mb.jsglr2.common;

import mb.common.util.ClassLoaderObjectInputStream;
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
import java.io.ObjectInputStream;

public class Jsglr2ParseTable {
    final IParseTable parseTable;

    private Jsglr2ParseTable(IParseTable parseTable) {
        this.parseTable = parseTable;
    }

    public static Jsglr2ParseTable fromStream(
        InputStream parseTableAtermStream,
        InputStream parseTablePersistedStream
    ) throws Jsglr2ParseTableException {
        final ITermFactory termFactory = new ImploderOriginTermFactory(new TermFactory());
        final TermReader reader = new TermReader(termFactory);
        try {
            final IStrategoTerm parseTableTerm = reader.parseFromStream(parseTableAtermStream);
            final IParseTable parseTable = new ParseTableReader().read(parseTableTerm);
            if((parseTable.totalStates() == 0 || parseTable.isLayoutSensitive())) {
                // Read serialized table when generation is dynamic (#states = 0) or when layout-sensitive.
                try(final ObjectInputStream ois = new ClassLoaderObjectInputStream(Jsglr2ParseTable.class.getClassLoader(), parseTablePersistedStream)) {
                    return new Jsglr2ParseTable((IParseTable)ois.readObject());
                }
            } else {
                return new Jsglr2ParseTable(parseTable);
            }
        } catch(IOException | ParseTableReadException | ClassNotFoundException e) {
            throw new Jsglr2ParseTableException("Loading parse table from stream failed unexpectedly", e);
        }
    }

    public static Jsglr2ParseTable fromParseTable(IParseTable parseTable) {
        return new Jsglr2ParseTable(parseTable);
    }
}
