package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerParseTable;
import mb.tiger.TigerParser;
import mb.tiger.TigerStyler;
import mb.tiger.TigerStylingRules;
import mb.tiger.spoofax.taskdef.AstTaskDef;
import mb.tiger.spoofax.taskdef.MessagesTaskDef;
import mb.tiger.spoofax.taskdef.StylingTaskDef;
import mb.tiger.spoofax.taskdef.TokenizerTaskDef;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Module that provides a Tiger parser, styler, and task definitions.
 */
@Module
public class TigerModule {
    private final TigerParser parser;
    private final TigerStyler styler;

    private TigerModule(TigerParseTable parseTable, TigerStylingRules stylingRules) {
        this.parser = new TigerParser(parseTable);
        this.styler = new TigerStyler(stylingRules);
    }

    public static TigerModule fromClassLoaderResources() throws JSGLR1ParseTableException, IOException {
        final TigerParseTable parseTable = TigerParseTable.fromClassLoaderResources();
        final TigerStylingRules stylingRules = TigerStylingRules.fromClassLoaderResources();
        return new TigerModule(parseTable, stylingRules);
    }

    @LanguageScope @Provides TigerParser provideParser() {
        return parser;
    }

    @LanguageScope @Provides TigerStyler provideStyler() {
        return styler;
    }

    @LanguageScope @Provides @ElementsIntoSet Set<TaskDef<?, ?>> provideTaskDefs(
        MessagesTaskDef messagesTaskDef,
        AstTaskDef astTaskDef,
        TokenizerTaskDef tokenizerTaskDef,
        StylingTaskDef stylingTaskDef
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(messagesTaskDef);
        taskDefs.add(astTaskDef);
        taskDefs.add(tokenizerTaskDef);
        taskDefs.add(stylingTaskDef);
        return taskDefs;
    }
}
