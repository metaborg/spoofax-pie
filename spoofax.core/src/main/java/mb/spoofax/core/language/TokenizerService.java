package mb.spoofax.core.language;

import mb.common.token.Token;
import mb.fs.api.path.FSPath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;

public interface TokenizerService {
    @Nullable ArrayList<Token> getTokens(FSPath path);
}
