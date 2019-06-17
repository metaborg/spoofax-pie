package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.log.api.LoggerFactory;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.PieSession;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.stratego.common.StrategoRuntimeBuilderException;
import mb.tiger.TigerConstraintAnalyzer;
import mb.tiger.TigerNaBL2StrategoRuntimeBuilder;
import mb.tiger.TigerParseTable;
import mb.tiger.TigerStrategoRuntimeBuilder;
import mb.tiger.TigerStyler;
import mb.tiger.TigerStylingRules;
import mb.tiger.spoofax.taskdef.TigerAnalyze;
import mb.tiger.spoofax.taskdef.TigerCheck;
import mb.tiger.spoofax.taskdef.TigerGetAST;
import mb.tiger.spoofax.taskdef.TigerParse;
import mb.tiger.spoofax.taskdef.TigerStyle;
import mb.tiger.spoofax.taskdef.TigerTokenize;

import javax.inject.Named;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Module that provides a Tiger parser, styler, and task definitions.
 */
@Module
public class TigerModule {
    private final TigerParseTable parseTable;
    private final TigerStylingRules stylingRules;
    private final StrategoRuntimeBuilder strategoRuntimeBuilder;
    private final TigerConstraintAnalyzer constraintAnalyzer;

    private TigerModule(
        TigerParseTable parseTable,
        TigerStylingRules stylingRules,
        StrategoRuntimeBuilder strategoRuntimeBuilder,
        TigerConstraintAnalyzer constraintAnalyzer
    ) {
        this.parseTable = parseTable;
        this.stylingRules = stylingRules;
        this.strategoRuntimeBuilder = strategoRuntimeBuilder;
        this.constraintAnalyzer = constraintAnalyzer;
    }

    public static TigerModule fromClassLoaderResources() throws JSGLR1ParseTableException, IOException, StrategoRuntimeBuilderException {
        final TigerParseTable parseTable = TigerParseTable.fromClassLoaderResources();
        final TigerStylingRules stylingRules = TigerStylingRules.fromClassLoaderResources();
        final StrategoRuntimeBuilder strategoRuntimeBuilder = TigerStrategoRuntimeBuilder.fromClassLoaderResources();
        final TigerConstraintAnalyzer constraintAnalyzer =
            new TigerConstraintAnalyzer(TigerNaBL2StrategoRuntimeBuilder.create(strategoRuntimeBuilder).build());
        return new TigerModule(parseTable, stylingRules, strategoRuntimeBuilder, constraintAnalyzer);
    }


    @Provides @LanguageScope
    LanguageInstance provideLanguageInstance(TigerInstance tigerInstance) { return tigerInstance; }


    @Provides @LanguageScope
    TigerParseTable provideParseTable() {
        return parseTable;
    }

    @Provides @LanguageScope
    TigerStyler provideStyler(LoggerFactory loggerFactory) {
        return new TigerStyler(stylingRules, loggerFactory);
    }

    @Provides @LanguageScope
    StrategoRuntimeBuilder provideStrategoRuntimeBuilder() {
        return strategoRuntimeBuilder;
    }

    @Provides @LanguageScope
    TigerConstraintAnalyzer provideConstraintAnalyzer() {
        return constraintAnalyzer;
    }


    @Provides @LanguageScope @Named("language") @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        TigerParse parse,
        TigerTokenize tokenize,
        TigerGetAST getAst,
        TigerStyle style,
        TigerAnalyze analyze,
        TigerCheck check
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(parse);
        taskDefs.add(tokenize);
        taskDefs.add(getAst);
        taskDefs.add(style);
        taskDefs.add(analyze);
        taskDefs.add(check);
        return taskDefs;
    }

    @Provides @LanguageScope @Named("language")
    TaskDefs provideTaskDefs(@Named("language") Set<TaskDef<?, ?>> taskDefs) {
        return new MapTaskDefs(taskDefs);
    }


    @Provides /* Unscoped: new session every call. */
    PieSession providePieSession(Pie pie, @Named("language") TaskDefs languageTaskDefs) {
        return pie.newSession(languageTaskDefs);
    }
}
