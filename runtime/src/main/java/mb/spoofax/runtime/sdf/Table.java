package mb.spoofax.runtime.sdf;

import mb.pie.vfs.path.PPath;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.terms.io.binary.TermReader;

import java.io.*;
import java.util.Arrays;

public class Table implements Serializable {
    private static final long serialVersionUID = 1L;

    private final byte[] data;
    private transient ParseTable cachedParseTable = null;


    public Table(byte[] data) {
        this.data = data;
    }

    public Table(PPath path) throws IOException {
        this.data = path.readAllBytes();
    }


    public Parser createParser(ITermFactory termFactory) throws IOException {
        final ParseTable parseTable = createParseTable(termFactory);
        return new Parser(parseTable, termFactory);
    }

    private ParseTable createParseTable(ITermFactory termFactory) throws IOException {
        if(cachedParseTable != null) {
            return cachedParseTable;
        }

        final TermReader reader = new TermReader(termFactory);
        try(final InputStream stream = new ByteArrayInputStream(data)) {
            final IStrategoTerm parseTableTerm = reader.parseFromStream(new ByteArrayInputStream(data));
            cachedParseTable = new ParseTable(parseTableTerm, termFactory);
        } catch(Exception e) {
            throw new IOException("Could not load parse table from stream", e);
        }

        return cachedParseTable;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(data);
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Table other = (Table) obj;
        return Arrays.equals(data, other.data);
    }
}
