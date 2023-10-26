package mb.dynamix_runtime;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.ReadableResource;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;

import java.io.IOException;
import java.io.InputStream;

public abstract class ReadDynamixSpecTaskDef implements TaskDef<None, Result<IStrategoTerm, ?>> {
    private final ITermFactory termFactory;

    protected ReadDynamixSpecTaskDef(
        ITermFactory termFactory
    ) {
        this.termFactory = termFactory;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public abstract Result<IStrategoTerm, ?> exec(ExecContext context, None input) throws Exception;

    /**
     * Attempt to parse the given readable resource into an ATerm.
     */
    protected Result<IStrategoTerm, ?> readToTerm(ReadableResource resource) {
        try {
            final IStrategoTerm specAst;
            try(InputStream inputStream = resource.openRead()) {
                specAst = readTerm(inputStream);
            }
            return Result.ofOk(specAst);
        } catch(Exception ex) {
            return Result.ofErr(ex);
        }
    }

    private IStrategoTerm readTerm(InputStream stream) {
        final TermReader reader = new TermReader(termFactory);
        try {
            return reader.parseFromStream(stream);
        } catch(IOException e) {
            throw new IllegalStateException("Loading ATerm from stream failed unexpectedly", e);
        }
    }
}
