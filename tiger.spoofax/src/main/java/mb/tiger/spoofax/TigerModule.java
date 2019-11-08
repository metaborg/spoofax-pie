package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.log.api.LoggerFactory;
import mb.pie.api.*;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.tiger.*;
import mb.tiger.spoofax.taskdef.*;
import mb.tiger.spoofax.taskdef.command.*;

import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

@Module
public class TigerModule {
    @Provides @LanguageScope
    TigerParserFactory provideParserFactory() {
        return new TigerParserFactory();
    }

    @Provides /* Unscoped: parser has state, so create a new parser every call. */
    TigerParser provideParser(TigerParserFactory parserFactory) {
        return parserFactory.create();
    }


    @Provides @LanguageScope
    TigerStylerFactory provideStylerFactory(LoggerFactory loggerFactory) {
        return new TigerStylerFactory(loggerFactory);
    }

    @Provides @LanguageScope
    TigerStyler provideStyler(TigerStylerFactory stylerFactory) {
        return stylerFactory.create();
    }


    @Provides @LanguageScope
    TigerStrategoRuntimeBuilderFactory provideStrategoRuntimeBuilderFactory() {
        return new TigerStrategoRuntimeBuilderFactory();
    }

    @Provides @LanguageScope
    StrategoRuntimeBuilder provideStrategoRuntimeBuilder(TigerStrategoRuntimeBuilderFactory factory) {
        return factory.create();
    }

    @Provides @LanguageScope
    StrategoRuntime providePrototypeStrategoRuntime(StrategoRuntimeBuilder builder) {
        return builder.build();
    }


    @Provides @LanguageScope
    TigerConstraintAnalyzerFactory provideConstraintAnalyzerFactory(StrategoRuntime prototypeStrategoRuntime) {
        return new TigerConstraintAnalyzerFactory(prototypeStrategoRuntime);
    }

    @Provides @LanguageScope
    TigerConstraintAnalyzer provideConstraintAnalyzer(TigerConstraintAnalyzerFactory factory) {
        return factory.create();
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


    @Provides @LanguageScope
    LanguageInstance provideLanguageInstance(TigerInstance tigerInstance) {
        return tigerInstance;
    }

    @Provides /* Unscoped: new session every call. */
    PieSession providePieSession(Pie pie, @Named("language") TaskDefs languageTaskDefs) {
        return pie.newSession(languageTaskDefs);
    }
}
