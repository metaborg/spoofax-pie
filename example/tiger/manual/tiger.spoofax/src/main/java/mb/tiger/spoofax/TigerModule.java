package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.log.api.LoggerFactory;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.PieSession;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import mb.resource.ResourceService;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.LanguageScope;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.tiger.TigerConstraintAnalyzer;
import mb.tiger.TigerConstraintAnalyzerFactory;
import mb.tiger.TigerParser;
import mb.tiger.TigerParserFactory;
import mb.tiger.TigerStrategoRuntimeBuilderFactory;
import mb.tiger.TigerStyler;
import mb.tiger.TigerStylerFactory;
import mb.tiger.spoofax.taskdef.TigerAnalyze;
import mb.tiger.spoofax.taskdef.TigerGetMessages;
import mb.tiger.spoofax.taskdef.TigerGetParsedTokens;
import mb.tiger.spoofax.taskdef.TigerListDefNames;
import mb.tiger.spoofax.taskdef.TigerListLiteralVals;
import mb.tiger.spoofax.taskdef.TigerParse;
import mb.tiger.spoofax.taskdef.TigerStyle;
import mb.tiger.spoofax.taskdef.command.TigerAltCompileFile;
import mb.tiger.spoofax.taskdef.command.TigerCompileDirectory;
import mb.tiger.spoofax.taskdef.command.TigerCompileFile;
import mb.tiger.spoofax.taskdef.command.TigerShowAnalyzedAst;
import mb.tiger.spoofax.taskdef.command.TigerShowDesugaredAst;
import mb.tiger.spoofax.taskdef.command.TigerShowParsedAst;
import mb.tiger.spoofax.taskdef.command.TigerShowPrettyPrintedText;

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
    StrategoRuntimeBuilder provideStrategoRuntimeBuilder(TigerStrategoRuntimeBuilderFactory factory, LoggerFactory loggerFactory, ResourceService resourceService) {
        return factory.create(loggerFactory, resourceService);
    }

    @Provides @LanguageScope
    StrategoRuntime providePrototypeStrategoRuntime(StrategoRuntimeBuilder builder) {
        return builder.build();
    }


    @Provides @LanguageScope
    TigerConstraintAnalyzerFactory provideConstraintAnalyzerFactory(LoggerFactory loggerFactory, ResourceService resourceService, StrategoRuntime prototypeStrategoRuntime) {
        return new TigerConstraintAnalyzerFactory(loggerFactory, resourceService, prototypeStrategoRuntime);
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

        TigerGetParsedTokens tokenize,
        TigerStyle style,
        TigerGetMessages check,

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
