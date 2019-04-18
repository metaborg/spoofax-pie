package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.PieSession;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import mb.spoofax.core.language.LanguageScope;
import mb.tiger.TigerParseTable;
import mb.tiger.TigerParser;
import mb.tiger.TigerStyler;
import mb.tiger.TigerStylingRules;
import mb.tiger.spoofax.taskdef.AstTaskDef;
import mb.tiger.spoofax.taskdef.MessagesTaskDef;
import mb.tiger.spoofax.taskdef.ParseTaskDef;
import mb.tiger.spoofax.taskdef.StylingTaskDef;
import mb.tiger.spoofax.taskdef.TokenizerTaskDef;

import javax.inject.Named;
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


    @Provides @LanguageScope TigerParser provideParser() {
        return parser;
    }

    @Provides @LanguageScope TigerStyler provideStyler() {
        return styler;
    }

    @Provides @LanguageScope @Named("language") @ElementsIntoSet static Set<TaskDef<?, ?>> provideTaskDefsSet(
        ParseTaskDef parseTaskDef,
        MessagesTaskDef messagesTaskDef,
        AstTaskDef astTaskDef,
        TokenizerTaskDef tokenizerTaskDef,
        StylingTaskDef stylingTaskDef
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(parseTaskDef);
        taskDefs.add(messagesTaskDef);
        taskDefs.add(astTaskDef);
        taskDefs.add(tokenizerTaskDef);
        taskDefs.add(stylingTaskDef);
        return taskDefs;
    }

    @Provides @LanguageScope @Named("language") TaskDefs provideTaskDefs(
        @Named("language") Set<TaskDef<?, ?>> taskDefs
    ) {
        return new MapTaskDefs(taskDefs);
    }

    // Unscoped: new session every call.
    @Provides PieSession providePieSession(Pie pie, @Named("language") TaskDefs languageTaskDefs) {
        return pie.newSession(languageTaskDefs);
    }
}
