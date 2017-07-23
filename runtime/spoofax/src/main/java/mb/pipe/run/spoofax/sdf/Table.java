package mb.pipe.run.spoofax.sdf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.terms.io.binary.TermReader;

import mb.vfs.path.PPath;

public class Table implements Serializable {
    private static final long serialVersionUID = 1L;

    private final byte[] data;
    private transient ParseTable cachedParseTable = null;


    public Table(byte[] data) {
        this.data = data;
    }

    public Table(PPath path) throws IOException {
        this.data = Files.readAllBytes(path.getJavaPath());
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
        if(!Arrays.equals(data, other.data))
            return false;
        return true;
    }
}
