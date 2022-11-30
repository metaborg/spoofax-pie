package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.log.api.LoggerFactory;
import mb.pie.api.TaskDef;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.HierarchicalResourceType;
import mb.statix.referenceretention.pie.InlineMethodCallTaskDef;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.tiger.TigerConstraintAnalyzer;
import mb.tiger.TigerConstraintAnalyzerFactory;
import mb.tiger.TigerParser;
import mb.tiger.TigerParserFactory;
import mb.tiger.TigerStrategoRuntimeBuilderFactory;
import mb.tiger.TigerStyler;
import mb.tiger.TigerStylerFactory;
import mb.tiger.spoofax.command.TigerCompileDirectoryCommand;
import mb.tiger.spoofax.command.TigerCompileFileAltCommand;
import mb.tiger.spoofax.command.TigerCompileFileCommand;
import mb.tiger.spoofax.command.TigerInlineMethodCallCommand;
import mb.tiger.spoofax.command.TigerShowAnalyzedAstCommand;
import mb.tiger.spoofax.command.TigerShowDesugaredAstCommand;
import mb.tiger.spoofax.command.TigerShowParsedAstCommand;
import mb.tiger.spoofax.command.TigerShowPrettyPrintedTextCommand;
import mb.tiger.spoofax.command.TigerShowScopeGraphCommand;
import mb.tiger.spoofax.task.TigerCheck;
import mb.tiger.spoofax.task.TigerCheckAggregator;
import mb.tiger.spoofax.task.TigerCompileDirectory;
import mb.tiger.spoofax.task.TigerCompileFile;
import mb.tiger.spoofax.task.TigerCompileFileAlt;
import mb.tiger.spoofax.task.TigerGetSourceFiles;
import mb.tiger.spoofax.task.TigerIdeTokenize;
import mb.tiger.spoofax.task.TigerInlineMethodCall;
import mb.tiger.spoofax.task.TigerShowAnalyzedAst;
import mb.tiger.spoofax.task.TigerShowDesugaredAst;
import mb.tiger.spoofax.task.TigerShowParsedAst;
import mb.tiger.spoofax.task.TigerShowPrettyPrintedText;
import mb.tiger.spoofax.task.TigerShowScopeGraph;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerListDefNames;
import mb.tiger.spoofax.task.reusable.TigerListLiteralVals;
import mb.tiger.spoofax.task.reusable.TigerParse;
import mb.tiger.spoofax.task.reusable.TigerStyle;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

@Module
public class TigerModule {
    @Provides @TigerScope
    static TigerParserFactory provideParserFactory(@TigerQualifier("definition-directory") HierarchicalResource definitionDir) {
        return new TigerParserFactory(definitionDir);
    }

    @Provides /* Unscoped: parser has state, so create a new parser every call. */
    static TigerParser provideParser(TigerParserFactory parserFactory) {
        return parserFactory.create();
    }

    @Provides @Named("packageId") String providePackageId() {
        return "mb.tiger.spoofax";
    }

    @Provides @TigerScope
    static TigerStylerFactory provideStylerFactory(LoggerFactory loggerFactory, @TigerQualifier("definition-directory") HierarchicalResource definitionDir) {
        return new TigerStylerFactory(loggerFactory, definitionDir);
    }

    @Provides @TigerScope
    static TigerStyler provideStyler(TigerStylerFactory stylerFactory) {
        return stylerFactory.create();
    }

    @Provides @TigerScope
    static TigerStrategoRuntimeBuilderFactory provideStrategoRuntimeBuilderFactory(LoggerFactory loggerFactory, ResourceService resourceService, @TigerQualifier("definition-directory") HierarchicalResource definitionDir) {
        return new TigerStrategoRuntimeBuilderFactory(loggerFactory, resourceService, definitionDir);
    }

    @Provides @TigerScope
    static StrategoRuntimeBuilder provideStrategoRuntimeBuilder(TigerStrategoRuntimeBuilderFactory factory) {
        return factory.create();
    }

    @Provides @TigerScope @Named("prototype")
    static StrategoRuntime providePrototypeStrategoRuntime(StrategoRuntimeBuilder builder) {
        return builder.build();
    }

    @Provides /* Unscoped: new session every call. */
    static StrategoRuntime provideStrategoRuntime(StrategoRuntimeBuilder builder, @Named("prototype") StrategoRuntime prototype) {
        return builder.buildFromPrototype(prototype);
    }

    @Provides @TigerScope
    static mb.tego.strategies.runtime.TegoRuntime provideTegoRuntime(mb.tego.strategies.runtime.TegoRuntimeImpl tegoImplementation) {
        return tegoImplementation;
    }

    @Provides @TigerScope
    static TigerConstraintAnalyzerFactory provideConstraintAnalyzerFactory(ResourceService resourceService) {
        return new TigerConstraintAnalyzerFactory(resourceService);
    }

    @Provides @TigerScope
    static TigerConstraintAnalyzer provideConstraintAnalyzer(TigerConstraintAnalyzerFactory factory) {
        return factory.create();
    }


    @Provides @TigerScope
    static ITermFactory provideTermFactory() {
        return new org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory(new TermFactory());
    }

    @Provides @TigerScope
    static mb.nabl2.terms.stratego.StrategoTerms provideStrategoTerms(ITermFactory termFactory) {
        return new mb.nabl2.terms.stratego.StrategoTerms(termFactory);
    }


    @Provides @TigerScope @TigerQualifier @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        TigerParse parse,
        TigerStyle style,
        TigerAnalyze analyze,

        TigerListLiteralVals listLiteralVals,
        TigerListDefNames listDefNames,

        TigerIdeTokenize tokenize,
        TigerGetSourceFiles getSourceFiles,
        TigerCheck check,
        TigerCheckAggregator checkAggregate,

        TigerShowParsedAst showParsedAst,
        TigerShowPrettyPrintedText showPrettyPrintedText,
        TigerShowAnalyzedAst showAnalyzedAst,
        TigerShowScopeGraph showScopeGraph,
        TigerShowDesugaredAst showDesugaredAst,

        TigerInlineMethodCall inlineMethodCall,
        InlineMethodCallTaskDef inlineMethodCallTaskDef,

        TigerCompileFile compileFile,
        TigerCompileFileAlt compileFileAlt,
        TigerCompileDirectory compileDirectory
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();

        taskDefs.add(parse);
        taskDefs.add(style);
        taskDefs.add(analyze);

        taskDefs.add(listLiteralVals);
        taskDefs.add(listDefNames);

        taskDefs.add(tokenize);
        taskDefs.add(getSourceFiles);
        taskDefs.add(check);
        taskDefs.add(checkAggregate);

        taskDefs.add(showParsedAst);
        taskDefs.add(showPrettyPrintedText);
        taskDefs.add(showAnalyzedAst);
        taskDefs.add(showScopeGraph);
        taskDefs.add(showDesugaredAst);

        taskDefs.add(inlineMethodCall);
        taskDefs.add(inlineMethodCallTaskDef);

        taskDefs.add(compileFile);
        taskDefs.add(compileFileAlt);
        taskDefs.add(compileDirectory);

        return taskDefs;
    }

    @Provides @TigerScope @ElementsIntoSet
    static Set<CommandDef<?>> provideCommandDefsSet(
        TigerShowParsedAstCommand showParsedAstCommand,
        TigerShowDesugaredAstCommand showDesugaredAstCommand,
        TigerShowAnalyzedAstCommand showAnalyzedAstCommand,
        TigerShowScopeGraphCommand showScopeGraphCommand,
        TigerShowPrettyPrintedTextCommand showPrettyPrintedTextCommand,

        TigerInlineMethodCallCommand inlineMethodCallCommand,

        TigerCompileFileCommand compileFileCommand,
        TigerCompileDirectoryCommand compileDirectoryCommand,
        TigerCompileFileAltCommand compileFileAltCommand
    ) {
        final HashSet<CommandDef<?>> commandDefs = new HashSet<>();

        commandDefs.add(showParsedAstCommand);
        commandDefs.add(showDesugaredAstCommand);
        commandDefs.add(showAnalyzedAstCommand);
        commandDefs.add(showScopeGraphCommand);
        commandDefs.add(showPrettyPrintedTextCommand);

        commandDefs.add(inlineMethodCallCommand);

        commandDefs.add(compileFileCommand);
        commandDefs.add(compileDirectoryCommand);
        commandDefs.add(compileFileAltCommand);

        return commandDefs;
    }

    @Provides @TigerScope @ElementsIntoSet
    static Set<AutoCommandRequest<?>> provideAutoCommandRequestsSet(
        TigerCompileFileCommand tigerCompileFileCommand,
        TigerCompileDirectoryCommand tigerCompileDirectoryCommand
    ) {
        final HashSet<AutoCommandRequest<?>> autoCommandDefs = new HashSet<>();
        autoCommandDefs.add(AutoCommandRequest.of(tigerCompileFileCommand, HierarchicalResourceType.File));
        autoCommandDefs.add(AutoCommandRequest.of(tigerCompileDirectoryCommand, HierarchicalResourceType.Directory));
        return autoCommandDefs;
    }


    @Provides @TigerScope
    static LanguageInstance provideLanguageInstance(TigerInstance tigerInstance) {
        return tigerInstance;
    }
}
