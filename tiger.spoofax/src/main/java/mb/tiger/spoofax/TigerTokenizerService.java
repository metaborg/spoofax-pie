package mb.tiger.spoofax;

import mb.common.token.Token;
import mb.fs.api.path.FSPath;
import mb.jsglr1.common.JSGLR1ParseOutput;
import mb.pie.api.ExecException;
import mb.pie.api.exec.TopDownExecutor;
import mb.spoofax.core.language.TokenizerService;
import mb.tiger.spoofax.pie.ParseTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;

public class TigerTokenizerService implements TokenizerService {
    private final TopDownExecutor topDownExecutor;
    private final ParseTaskDef parseTaskDef;

    @Inject public TigerTokenizerService(TopDownExecutor topDownExecutor, ParseTaskDef parseTaskDef) {
        this.topDownExecutor = topDownExecutor;
        this.parseTaskDef = parseTaskDef;
    }

    @Override public @Nullable ArrayList<Token> getTokens(FSPath path) {
        try {
            final @Nullable JSGLR1ParseOutput parseOutput =
                topDownExecutor.newSession().requireInitial(parseTaskDef.createTask(path));
            if(parseOutput == null) {
                return null;
            }
            return parseOutput.tokens;
        } catch(ExecException e) {
            throw new RuntimeException("Getting tokens for path '" + path + "' failed unexpectedly", e.getCause());
        }
    }
}
