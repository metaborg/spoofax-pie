package mb.sdf3.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.OutTransient;
import mb.pie.api.OutTransientImpl;
import mb.pie.api.TaskDef;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.sdf3.Sdf3Scope;
import org.metaborg.parsetable.IParseTable;
import org.metaborg.parsetable.ParseTableReadException;
import org.metaborg.parsetable.ParseTableReader;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

@Sdf3Scope
public class Sdf3ParseTableFromFile implements TaskDef<ResourceKey, OutTransient<Result<IParseTable, ?>>> {
    private final ParseTableReader parseTableReader = new ParseTableReader();
    private final TermReader termReader = new TermReader(new TermFactory());

    @Inject public Sdf3ParseTableFromFile() {}

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public OutTransient<Result<IParseTable, ?>> exec(ExecContext context, ResourceKey input) {
        try(
            final ReadableResource file = context.require(input);
            final InputStream atermInputStream = file.openRead()
        ) {
            final IStrategoTerm term = termReader.read(atermInputStream);
            final IParseTable parseTable = parseTableReader.read(term);
            return new OutTransientImpl<>(Result.ofOk(parseTable), true);
        } catch(IOException | ParseTableReadException e) {
            return new OutTransientImpl<>(Result.ofErr(e), true);
        }
    }
}
