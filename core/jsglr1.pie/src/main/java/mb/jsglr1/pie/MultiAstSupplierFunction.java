package mb.jsglr1.pie;

import mb.common.result.Result;
import mb.common.util.MapView;
import mb.jsglr1.common.JSGLR1ParseException;
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

public class MultiAstSupplierFunction implements Function<ResourcePath, MapView<ResourceKey, Supplier<Result<IStrategoTerm, JSGLR1ParseException>>>> {
    private final Function<JSGLR1ParseTaskInput, Result<IStrategoTerm, JSGLR1ParseException>> parseToAstFunction;
    private final ResourceWalker walker;
    private final ResourceMatcher matcher;

    public MultiAstSupplierFunction(Function<JSGLR1ParseTaskInput, Result<IStrategoTerm, JSGLR1ParseException>> parseToAstFunction, ResourceWalker walker, ResourceMatcher matcher) {
        this.parseToAstFunction = parseToAstFunction;
        this.walker = walker;
        this.matcher = matcher;
    }

    @Override
    public MapView<ResourceKey, Supplier<Result<IStrategoTerm, JSGLR1ParseException>>> apply(ExecContext context, ResourcePath rootDirectory) {
        final HashMap<ResourceKey, Supplier<Result<IStrategoTerm, JSGLR1ParseException>>> astsAndErrors = new HashMap<>();
        try(final Stream<? extends HierarchicalResource> stream = context.require(rootDirectory, ResourceStampers.modifiedDirRec(walker, matcher)).walk(walker, matcher)) {
            stream.forEach(file -> astsAndErrors.put(file.getKey(), parseToAstFunction.createSupplier(JSGLR1ParseTaskInput.builder().withFile(file.getKey()).rootDirectoryHint(rootDirectory).build())));
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
        return MapView.of(astsAndErrors);
    }
}
