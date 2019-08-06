package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.jsglr1.common.JSGLR1ParseTableException;
import mb.log.api.LoggerFactory;
import mb.pie.api.*;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.stratego.common.StrategoRuntimeBuilderException;
import mb.tiger.*;
import mb.tiger.spoofax.taskdef.*;
import mb.tiger.spoofax.taskdef.command.*;

import javax.inject.Named;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Module
public class TigerModule {
    private final TigerParseTable parseTable;
    private final TigerStylingRules stylingRules;
    private final StrategoRuntimeBuilder strategoRuntimeBuilder;
    private final StrategoRuntime prototypeStrategoRuntime;
    private final TigerConstraintAnalyzer constraintAnalyzer;

    private TigerModule(
        TigerParseTable parseTable,
        TigerStylingRules stylingRules,
        StrategoRuntimeBuilder strategoRuntimeBuilder,
        StrategoRuntime prototypeStrategoRuntime,
        TigerConstraintAnalyzer constraintAnalyzer
    ) {
        this.parseTable = parseTable;
        this.stylingRules = stylingRules;
        this.strategoRuntimeBuilder = strategoRuntimeBuilder;
        this.prototypeStrategoRuntime = prototypeStrategoRuntime;
        this.constraintAnalyzer = constraintAnalyzer;
    }

    public static TigerModule fromClassLoaderResources() throws JSGLR1ParseTableException, IOException, StrategoRuntimeBuilderException {
        final TigerParseTable parseTable = TigerParseTable.fromClassLoaderResources();
        final TigerStylingRules stylingRules = TigerStylingRules.fromClassLoaderResources();
        final StrategoRuntimeBuilder strategoRuntimeBuilder = TigerNaBL2StrategoRuntimeBuilder.create(TigerStrategoRuntimeBuilder.create());
        final StrategoRuntime prototypeStrategoRuntime = strategoRuntimeBuilder.build();
        final TigerConstraintAnalyzer constraintAnalyzer = new TigerConstraintAnalyzer(strategoRuntimeBuilder.buildFromPrototype(prototypeStrategoRuntime));
        return new TigerModule(parseTable, stylingRules, strategoRuntimeBuilder, prototypeStrategoRuntime, constraintAnalyzer);
    }


    @Provides @LanguageScope
    LanguageInstance provideLanguageInstance(TigerInstance tigerInstance) {
        return tigerInstance;
    }


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
    StrategoRuntime providePrototypeStrategoRuntime() {
        return prototypeStrategoRuntime;
    }

    @Provides @LanguageScope
    TigerConstraintAnalyzer provideConstraintAnalyzer() {
        return constraintAnalyzer;
    }


    @Provides @LanguageScope @Named("language") @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        TigerParse parse,
        TigerAnalyze analyze,

        TigerListLiteralVals listLiteralVals,
        TigerListDefNames listDefNames,

        TigerTokenize tokenize,
        TigerStyle style,
        TigerCheck check,

        TigerShowParsedAst showParsedAst,
        TigerShowPrettyPrintedText showPrettyPrintedText,
        TigerShowAnalyzedAst showAnalyzedAst,
        TigerShowDesugaredAst showDesugaredAst,
        TigerCompileFile compileFile,
        TigerAltCompileFile altCompileFile,
        TigerCompileDirectory compileDirectory
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();

        taskDefs.add(parse);
        taskDefs.add(analyze);

        taskDefs.add(listLiteralVals);
        taskDefs.add(listDefNames);

        taskDefs.add(tokenize);
        taskDefs.add(style);
        taskDefs.add(check);

        taskDefs.add(showParsedAst);
        taskDefs.add(showPrettyPrintedText);
        taskDefs.add(showAnalyzedAst);
        taskDefs.add(showDesugaredAst);
        taskDefs.add(compileFile);
        taskDefs.add(altCompileFile);
        taskDefs.add(compileDirectory);

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
