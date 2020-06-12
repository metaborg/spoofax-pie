package mb.tiger.spoofax.task.reusable;

import mb.common.style.Styling;
import mb.jsglr.common.JSGLRTokens;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerStyler;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;

@LanguageScope
public class TigerStyle implements TaskDef<Supplier<@Nullable JSGLRTokens>, @Nullable Styling> {
    private final TigerStyler styler;

    @Inject public TigerStyle(TigerStyler styler) {
        this.styler = styler;
    }

    @Override public String getId() {
        return "mb.tiger.spoofax.task.reusable.TigerStyle";
    }

    @Override
    public @Nullable Styling exec(
        ExecContext context,
        Supplier<@Nullable JSGLRTokens> tokensSupplier
    ) throws ExecException, IOException, InterruptedException {
        final @Nullable JSGLRTokens tokens = context.require(tokensSupplier);
        if(tokens == null) {
            return null;
        } else {
            return styler.style(tokens.tokens);
        }
    }
}
