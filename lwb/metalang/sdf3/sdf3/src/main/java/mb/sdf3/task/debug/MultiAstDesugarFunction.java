package mb.sdf3.task.debug;

import mb.common.result.Result;
import mb.common.util.MapView;
import mb.jsglr.common.JsglrParseException;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.HashMap;

public class MultiAstDesugarFunction implements Function<MapView<ResourceKey, Supplier<Result<IStrategoTerm, JsglrParseException>>>, MapView<ResourceKey, ? extends Supplier<? extends Result<IStrategoTerm, ?>>>> {
    private final Function<Supplier<? extends Result<IStrategoTerm, ?>>, Result<IStrategoTerm, ?>> desugar;

    public MultiAstDesugarFunction(Function<Supplier<? extends Result<IStrategoTerm, ?>>, Result<IStrategoTerm, ?>> desugar) {
        this.desugar = desugar;
    }

    @Override
    public MapView<ResourceKey, ? extends Supplier<? extends Result<IStrategoTerm, ?>>> apply(ExecContext context, MapView<ResourceKey, Supplier<Result<IStrategoTerm, JsglrParseException>>> output) {
        final HashMap<ResourceKey, Supplier<? extends Result<IStrategoTerm, ?>>> desugaredAstsAndErrors = new HashMap<>();
        output.forEach(entry -> {
            desugaredAstsAndErrors.put(entry.getKey(), desugar.createSupplier(entry.getValue()));
        });
        return MapView.of(desugaredAstsAndErrors);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final MultiAstDesugarFunction that = (MultiAstDesugarFunction)o;
        return desugar.equals(that.desugar);
    }

    @Override public int hashCode() {
        return desugar.hashCode();
    }

    @Override public String toString() {
        return "MultiAstDesugarFunction{" +
            "desugar=" + desugar +
            '}';
    }
}
