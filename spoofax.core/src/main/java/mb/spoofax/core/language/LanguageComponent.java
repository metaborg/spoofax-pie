package mb.spoofax.core.language;

import mb.common.message.MessageCollection;
import mb.common.style.Styling;
import mb.common.token.Token;
import mb.fs.api.path.FSPath;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.ArrayList;

@LanguageScope
public interface LanguageComponent {
    TaskDef<FSPath, @Nullable MessageCollection> messagesTaskDef();

    TaskDef<FSPath, @Nullable IStrategoTerm> astTaskDef();

    TaskDef<FSPath, @Nullable ArrayList<Token>> tokenizerTaskDef();

    TaskDef<FSPath, @Nullable Styling> stylingTaskDef();
}
