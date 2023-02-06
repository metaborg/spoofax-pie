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

    public Jsglr2ParseTable(IParseTable parseTable) {
        this.parseTable = parseTable;
    }

    /**
     * Create a JSGLR2 parse table from a parse table ATerm (.tbl file) stream or persisted (.bin file) stream. The
     * persisted stream will be used when the parse table is layout sensitive, or dynamic.
     *
     * @param parseTableAtermStream     Input stream to the parse table ATerm format (.tbl file).
     * @param parseTablePersistedStream Input stream to the parse table persisted format (.bin file).
     * @return JSGLR2 parse table
     * @throws Jsglr2ParseTableException when loading a parse table fails.
     */
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

    /**
     * Create a JSGLR2 parse table from a parse table ATerm (.tbl file) stream. This does not support layout-sensitive
     * parse tables, nor dynamic parse tables.
     *
     * @param parseTableAtermStream Input stream to the parse table ATerm format (.tbl file).
     * @return JSGLR2 parse table
     * @throws Jsglr2ParseTableException when loading a parse table fails.
     */
    public static Jsglr2ParseTable fromStream(
        InputStream parseTableAtermStream
    ) throws Jsglr2ParseTableException {
        final ITermFactory termFactory = new ImploderOriginTermFactory(new TermFactory());
        final TermReader reader = new TermReader(termFactory);
        try {
            final IStrategoTerm parseTableTerm = reader.parseFromStream(parseTableAtermStream);
            final IParseTable parseTable = new ParseTableReader().read(parseTableTerm);
            return new Jsglr2ParseTable(parseTable);
        } catch(IOException | ParseTableReadException e) {
            throw new Jsglr2ParseTableException("Loading parse table from stream failed unexpectedly", e);
        }
    }
}
