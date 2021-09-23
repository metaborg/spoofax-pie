package mb.jsglr.pie;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.jsglr.common.JsglrParseException;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.HashMap;

public class MultiAstSupplierFunction implements Function<ResourcePath, MapView<ResourceKey, Supplier<Result<IStrategoTerm, JsglrParseException>>>> {
    private final Function<ResourcePath, ? extends ListView<? extends ResourceKey>> sourceFilesFunction;
    private final Function<JsglrParseTaskInput, Result<IStrategoTerm, JsglrParseException>> parseToAstFunction;

    public MultiAstSupplierFunction(
        Function<ResourcePath, ? extends ListView<? extends ResourceKey>> sourceFilesFunction,
        Function<JsglrParseTaskInput, Result<IStrategoTerm, JsglrParseException>> parseToAstFunction
    ) {
        this.sourceFilesFunction = sourceFilesFunction;
        this.parseToAstFunction = parseToAstFunction;
    }

    @Override
    public MapView<ResourceKey, Supplier<Result<IStrategoTerm, JsglrParseException>>> apply(ExecContext context, ResourcePath rootDirectory) {
        final HashMap<ResourceKey, Supplier<Result<IStrategoTerm, JsglrParseException>>> astsAndErrors = new HashMap<>();
        final JsglrParseTaskInput.Builder parseInputBuilder = JsglrParseTaskInput.builder().rootDirectoryHint(rootDirectory);
        for(final ResourceKey file : context.require(sourceFilesFunction, rootDirectory)) {
            astsAndErrors.put(file, parseToAstFunction.createSupplier(parseInputBuilder.withFile(file).build()));
        }
        return MapView.of(astsAndErrors);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final MultiAstSupplierFunction that = (MultiAstSupplierFunction)o;
        if(!sourceFilesFunction.equals(that.sourceFilesFunction)) return false;
        return parseToAstFunction.equals(that.parseToAstFunction);
    }

    @Override public int hashCode() {
        int result = sourceFilesFunction.hashCode();
        result = 31 * result + parseToAstFunction.hashCode();
        return result;
    }

    @Override public String toString() {
        return "MultiAstSupplierFunction{" +
            "sourceFilesFunction=" + sourceFilesFunction +
            ", parseToAstFunction=" + parseToAstFunction +
            '}';
    }
}
