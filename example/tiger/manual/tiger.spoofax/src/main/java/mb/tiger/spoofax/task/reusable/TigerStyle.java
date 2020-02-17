package mb.tiger.spoofax.task.reusable;

import mb.common.style.Styling;
import mb.common.token.Token;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Provider;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerStyler;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;

@LanguageScope
public class TigerStyle implements TaskDef<Provider<@Nullable ArrayList<? extends Token<IStrategoTerm>>>, @Nullable Styling> {
    private final TigerStyler styler;

    @Inject public TigerStyle(TigerStyler styler) {
        this.styler = styler;
    }

    @Override public String getId() {
        return "mb.tiger.spoofax.task.TigerStyle";
    }

    @Override
    public @Nullable Styling exec(ExecContext context, Provider<@Nullable ArrayList<? extends Token<IStrategoTerm>>> tokensProvider) throws ExecException, IOException, InterruptedException {
        final @Nullable ArrayList<? extends Token<IStrategoTerm>> tokens = context.require(tokensProvider);
        //noinspection ConstantConditions
        if(tokens == null) {
            return null;
        } else {
            return styler.style(tokens);
        }
    }
}
