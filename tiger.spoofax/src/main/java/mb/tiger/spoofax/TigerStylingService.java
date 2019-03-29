package mb.tiger.spoofax;

import mb.common.style.Styling;
import mb.fs.api.path.FSPath;
import mb.pie.api.ExecException;
import mb.pie.api.exec.TopDownExecutor;
import mb.spoofax.core.language.StylingService;
import mb.tiger.spoofax.pie.StyleTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public class TigerStylingService implements StylingService {
    private final TopDownExecutor topDownExecutor;
    private final StyleTaskDef styleTaskDef;

    @Inject public TigerStylingService(TopDownExecutor topDownExecutor, StyleTaskDef styleTaskDef) {
        this.topDownExecutor = topDownExecutor;
        this.styleTaskDef = styleTaskDef;
    }

    @Override public @Nullable Styling getStyling(FSPath path) {
        try {
            final @Nullable Styling styling =
                topDownExecutor.newSession().requireInitial(styleTaskDef.createTask(path));
            return styling;
        } catch(ExecException e) {
            throw new RuntimeException("Getting styling for path '" + path + "' failed unexpectedly", e.getCause());
        }
    }
}
