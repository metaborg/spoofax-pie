package mb.tiger.spoofax.task.reusable;

import mb.common.result.Result;
import mb.common.style.Styling;
import mb.jsglr.common.JSGLRTokens;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerStyler;

import javax.inject.Inject;
import java.io.IOException;

@LanguageScope
public class TigerStyle implements TaskDef<Supplier<? extends Result<JSGLRTokens, ?>>, Result<Styling, ?>> {
    private final TigerStyler styler;

    @Inject public TigerStyle(TigerStyler styler) {
        this.styler = styler;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Styling, ?> exec(
        ExecContext context,
        Supplier<? extends Result<JSGLRTokens, ?>> tokensSupplier
    ) throws ExecException, IOException, InterruptedException {
        final Result<JSGLRTokens, ? extends Throwable> result = context.require(tokensSupplier);
        return result.map(t -> styler.style(t.tokens));
    }
}
