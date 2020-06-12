package mb.tiger.spoofax.task;

import mb.common.result.MessagesError;
import mb.common.result.Result;
import mb.jsglr.common.JSGLRTokens;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.spoofax.task.reusable.TigerParse;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@LanguageScope
public class TigerIdeTokenize implements TaskDef<ResourceKey, @Nullable JSGLRTokens> {
    private final TigerParse parse;

    @Inject public TigerIdeTokenize(TigerParse parse) {
        this.parse = parse;
    }

    @Override public String getId() {
        return "mb.tiger.spoofax.task.TigerTokenize";
    }

    @Override
    public @Nullable JSGLRTokens exec(ExecContext context, ResourceKey key) throws ExecException, InterruptedException {
        final Result<JSGLR1ParseOutput, MessagesError> parseResult = context.require(parse, new ResourceStringSupplier(key));
        return parseResult.mapOrNull(o -> o.tokens); // TODO: use Result.
    }
}
