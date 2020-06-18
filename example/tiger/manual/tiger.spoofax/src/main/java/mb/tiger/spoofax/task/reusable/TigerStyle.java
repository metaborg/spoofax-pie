package mb.tiger.spoofax.task.reusable;

import mb.common.option.Option;
import mb.common.style.Styling;
import mb.jsglr.common.JSGLRTokens;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerStyler;

import javax.inject.Inject;
import java.io.IOException;

@LanguageScope
public class TigerStyle implements TaskDef<Supplier<Option<JSGLRTokens>>, Option<Styling>> {
    private final TigerStyler styler;

    @Inject
    public TigerStyle(TigerStyler styler) { this.styler = styler; }

    @Override
    public String getId() { return getClass().getName(); }

    @Override
    public Option<Styling> exec(ExecContext context, Supplier<Option<JSGLRTokens>> tokens) throws IOException {
        return context.require(tokens).map(t -> styler.style(t.tokens));
    }
}
