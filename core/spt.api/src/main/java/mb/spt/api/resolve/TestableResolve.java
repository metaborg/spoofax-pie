package mb.spt.api.resolve;

import mb.common.editor.ReferenceResolutionResult;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.pie.api.Session;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface TestableResolve {
    Result<ReferenceResolutionResult, ?> testResolve(
        Session session,
        ResourceKey resource,
        Region region,
        @Nullable ResourcePath rootDirectoryHint
    ) throws InterruptedException;
}
