package mb.spt.api.stratego;

import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.Session;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spt.api.analyze.StrategoRunArgument;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface TestableStratego {
    Result<IStrategoTerm, ?> testRunStrategy(
        Session session,
        ResourceKey resource,
        String strategy,
        ListView<StrategoRunArgument> arguments,
        Option<Region> region,
        @Nullable ResourcePath rootDirectoryHint
    ) throws InterruptedException;
}
