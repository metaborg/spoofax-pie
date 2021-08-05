package mb.spt.api.analyze;

import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.Session;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface TestableAnalysis {
    Result<IStrategoTerm, ?> testAnalyze(Session session, ResourceKey resource, @Nullable ResourcePath rootDirectoryHint) throws InterruptedException;
}
