package mb.spoofax.lwb.compiler.sdf3;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.pie.api.Supplier;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.List;

@Value.Immutable
public interface Sdf3CompileOutput extends Serializable {
    class Builder extends ImmutableSdf3CompileOutput.Builder {}

    static Builder builder() { return new Builder(); }


    KeyedMessages messages();

    List<Supplier<Result<IStrategoTerm, ?>>> esvCompletionColorerAstSuppliers();
}
