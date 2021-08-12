package mb.jsglr.pie;

import mb.common.result.Result;
import mb.common.util.MapView;
import mb.jsglr.common.JsglrParseException;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.stream.Stream;

public class WalkingMultiAstSupplierFunction implements Function<ResourcePath, MapView<ResourceKey, Supplier<Result<IStrategoTerm, JsglrParseException>>>> {
    private final Function<JsglrParseTaskInput, Result<IStrategoTerm, JsglrParseException>> parseToAstFunction;
    private final ResourceWalker walker;
    private final ResourceMatcher matcher;

    public WalkingMultiAstSupplierFunction(Function<JsglrParseTaskInput, Result<IStrategoTerm, JsglrParseException>> parseToAstFunction, ResourceWalker walker, ResourceMatcher matcher) {
        this.parseToAstFunction = parseToAstFunction;
        this.walker = walker;
        this.matcher = matcher;
    }

    @Override
    public MapView<ResourceKey, Supplier<Result<IStrategoTerm, JsglrParseException>>> apply(ExecContext context, ResourcePath rootDirectory) {
        final HashMap<ResourceKey, Supplier<Result<IStrategoTerm, JsglrParseException>>> astsAndErrors = new HashMap<>();
        try(final Stream<? extends HierarchicalResource> stream = context.require(rootDirectory, ResourceStampers.modifiedDirRec(walker, matcher)).walk(walker, matcher)) {
            stream.forEach(file -> astsAndErrors.put(file.getKey(), parseToAstFunction.createSupplier(JsglrParseTaskInput.builder().withFile(file.getKey()).rootDirectoryHint(rootDirectory).build())));
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
        return MapView.of(astsAndErrors);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        WalkingMultiAstSupplierFunction that = (WalkingMultiAstSupplierFunction)o;

        if(!parseToAstFunction.equals(that.parseToAstFunction)) return false;
        if(!walker.equals(that.walker)) return false;
        return matcher.equals(that.matcher);
    }

    @Override public int hashCode() {
        int result = parseToAstFunction.hashCode();
        result = 31 * result + walker.hashCode();
        result = 31 * result + matcher.hashCode();
        return result;
    }
}
