package mb.spt.api.parse;

import mb.common.result.Result;
import mb.pie.api.Session;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface TestableParse {
    Result<ParseResult, ?> testParse(Session session, ResourceKey resource, @Nullable ResourcePath rootDirectoryHint) throws InterruptedException;

    Result<IStrategoTerm, ?> testParseToAterm(Session session, ResourceKey resource, @Nullable ResourcePath rootDirectoryHint) throws InterruptedException;
}
