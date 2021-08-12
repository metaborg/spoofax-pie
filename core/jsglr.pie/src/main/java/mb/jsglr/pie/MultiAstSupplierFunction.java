package mb.jsglr.pie;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.common.util.MapView;
import mb.jsglr.common.JsglrParseException;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;

public class MultiAstSupplierFunction implements Function<ResourcePath, MapView<ResourceKey, Supplier<Result<IStrategoTerm, JsglrParseException>>>> {
    private final Function<JsglrParseTaskInput, Result<IStrategoTerm, JsglrParseException>> parseToAstFunction;
    private final ListView<ResourceKey> files;

    public MultiAstSupplierFunction(Function<JsglrParseTaskInput, Result<IStrategoTerm, JsglrParseException>> parseToAstFunction, ListView<ResourceKey> files) {
        this.parseToAstFunction = parseToAstFunction;
        this.files = files;
    }

    @Override
    public MapView<ResourceKey, Supplier<Result<IStrategoTerm, JsglrParseException>>> apply(ExecContext context, ResourcePath rootDirectory) {
        final HashMap<ResourceKey, Supplier<Result<IStrategoTerm, JsglrParseException>>> astsAndErrors = new HashMap<>();

        for(final ResourceKey file : files) {
            try {
                context.require(file, ResourceStampers.modifiedFile());
            } catch(IOException e) {
                throw new UncheckedIOException(e);
            }

            astsAndErrors.put(file, parseToAstFunction.createSupplier(JsglrParseTaskInput.builder().withFile(file).rootDirectoryHint(rootDirectory).build()));
        }

        return MapView.of(astsAndErrors);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        MultiAstSupplierFunction that = (MultiAstSupplierFunction)o;

        if(!parseToAstFunction.equals(that.parseToAstFunction)) return false;
        return files.equals(that.files);
    }

    @Override public int hashCode() {
        int result = parseToAstFunction.hashCode();
        result = 31 * result + files.hashCode();
        return result;
    }
}
