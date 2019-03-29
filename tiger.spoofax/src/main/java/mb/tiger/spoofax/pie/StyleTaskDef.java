package mb.tiger.spoofax.pie;

import mb.common.style.Styling;
import mb.fs.api.path.FSPath;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.tiger.TigerStyler;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public class StyleTaskDef implements TaskDef<FSPath, @Nullable Styling> {
    private final ParseTaskDef parseTaskDef;
    private final TigerStyler styler;

    @Inject public StyleTaskDef(ParseTaskDef parseTaskDef, TigerStyler styler) {
        this.parseTaskDef = parseTaskDef;
        this.styler = styler;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public @Nullable Styling exec(ExecContext context, FSPath path) throws ExecException, InterruptedException {
        final @Nullable JSGLR1ParseOutput parseOutput = context.require(parseTaskDef, path);
        if(parseOutput == null || parseOutput.tokens == null) {
            return null;
        }
        return styler.style(parseOutput.tokens);
    }
}
