package mb.tiger.spoofax;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.log.api.LoggerFactory;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import mb.resource.ResourceService;
import mb.resource.classloader.ClassLoaderResource;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.core.language.LanguageInstance;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.AutoCommandRequest;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.HierarchicalResourceType;
import mb.spoofax.core.platform.Platform;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoRuntimeBuilder;
import mb.tiger.TigerClassloaderResources;
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
import mb.tiger.spoofax.command.TigerShowAnalyzedAstCommand;
import mb.tiger.spoofax.command.TigerShowDesugaredAstCommand;
import mb.tiger.spoofax.command.TigerShowParsedAstCommand;
import mb.tiger.spoofax.command.TigerShowPrettyPrintedTextCommand;
import mb.tiger.spoofax.command.TigerShowScopeGraphCommand;
import mb.tiger.spoofax.task.TigerCompileDirectory;
import mb.tiger.spoofax.task.TigerCompileFile;
import mb.tiger.spoofax.task.TigerCompileFileAlt;
import mb.tiger.spoofax.task.TigerIdeCheck;
import mb.tiger.spoofax.task.TigerIdeCheckAggregate;
import mb.tiger.spoofax.task.TigerIdeTokenize;
import mb.tiger.spoofax.task.TigerShowAnalyzedAst;
import mb.tiger.spoofax.task.TigerShowDesugaredAst;
import mb.tiger.spoofax.task.TigerShowParsedAst;
import mb.tiger.spoofax.task.TigerShowPrettyPrintedText;
import mb.tiger.spoofax.task.TigerShowScopeGraph;
import mb.tiger.spoofax.task.reusable.TigerAnalyze;
import mb.tiger.spoofax.task.reusable.TigerCompleteTaskDef;
import mb.tiger.spoofax.task.reusable.TigerListDefNames;
import mb.tiger.spoofax.task.reusable.TigerListLiteralVals;
import mb.tiger.spoofax.task.reusable.TigerParse;
import mb.tiger.spoofax.task.reusable.TigerStyle;

import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

@Module
public class TigerModule {
    @Provides @LanguageScope
    static ClassLoaderResourceRegistry provideClassLoaderResourceRegistry() {
        return TigerClassloaderResources.createClassLoaderResourceRegistry();
    }

    @Provides @LanguageScope
    static ResourceService provideResourceRegistry(@Platform ResourceService resourceService, ClassLoaderResourceRegistry classLoaderResourceRegistry) {
        return resourceService.createChild(classLoaderResourceRegistry);
    }

    @Provides @Named("definition-dir") @LanguageScope
    static ClassLoaderResource provideDefinitionDir(ClassLoaderResourceRegistry registry) {
        return TigerClassloaderResources.createDefinitionDir(registry);
    }

    @Provides @Named("definition-dir") @LanguageScope
    static HierarchicalResource provideDefinitionDirAsHierarchicalResource(@Named("definition-dir") ClassLoaderResource definitionDir) {
        return definitionDir;
    }


    @Provides @LanguageScope
    static TigerParserFactory provideParserFactory(@Named("definition-dir") HierarchicalResource definitionDir) {
        return new TigerParserFactory(definitionDir);
    }

    @Provides /* Unscoped: parser has state, so create a new parser every call. */
    static TigerParser provideParser(TigerParserFactory parserFactory) {
        return parserFactory.create();
    }


    @Provides @LanguageScope
    static TigerStylerFactory provideStylerFactory(LoggerFactory loggerFactory, @Named("definition-dir") HierarchicalResource definitionDir) {
        return new TigerStylerFactory(loggerFactory, definitionDir);
    }

    @Provides @LanguageScope
    static TigerStyler provideStyler(TigerStylerFactory stylerFactory) {
        return stylerFactory.create();
    }

    @Provides @LanguageScope
    static TigerStrategoRuntimeBuilderFactory provideStrategoRuntimeBuilderFactory(LoggerFactory loggerFactory, ResourceService resourceService, @Named("definition-dir") HierarchicalResource definitionDir) {
        return new TigerStrategoRuntimeBuilderFactory(loggerFactory, resourceService, definitionDir);
    }

    @Provides @LanguageScope
    static StrategoRuntimeBuilder provideStrategoRuntimeBuilder(TigerStrategoRuntimeBuilderFactory factory) {
        return factory.create();
    }

    @Provides @LanguageScope @Named("prototype")
    static StrategoRuntime providePrototypeStrategoRuntime(StrategoRuntimeBuilder builder) {
        return builder.build();
    }

    @Provides /* Unscoped: new session every call. */
    static StrategoRuntime provideStrategoRuntime(StrategoRuntimeBuilder builder, @Named("prototype") StrategoRuntime prototype) {
        return builder.buildFromPrototype(prototype);
    }


    @Provides @LanguageScope
    static TigerConstraintAnalyzerFactory provideConstraintAnalyzerFactory(LoggerFactory loggerFactory, ResourceService resourceService, StrategoRuntime prototypeStrategoRuntime) {
        return new TigerConstraintAnalyzerFactory(loggerFactory, resourceService, prototypeStrategoRuntime);
    }

    @Provides @LanguageScope
    static TigerConstraintAnalyzer provideConstraintAnalyzer(TigerConstraintAnalyzerFactory factory) {
        return factory.create();
    }


    @Provides @LanguageScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        TigerParse parse,
        TigerStyle style,
        TigerAnalyze analyze,
        TigerCompleteTaskDef complete,

        TigerListLiteralVals listLiteralVals,
        TigerListDefNames listDefNames,

        TigerIdeTokenize tokenize,
        TigerIdeCheck check,
        TigerIdeCheckAggregate checkAggregate,

        TigerShowParsedAst showParsedAst,
        TigerShowPrettyPrintedText showPrettyPrintedText,
        TigerShowAnalyzedAst showAnalyzedAst,
        TigerShowScopeGraph showScopeGraph,
        TigerShowDesugaredAst showDesugaredAst,

        TigerCompileFile compileFile,
        TigerCompileFileAlt compileFileAlt,
        TigerCompileDirectory compileDirectory
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();

        taskDefs.add(parse);
        taskDefs.add(style);
        taskDefs.add(analyze);
        taskDefs.add(complete);

        taskDefs.add(listLiteralVals);
        taskDefs.add(listDefNames);

        taskDefs.add(tokenize);
        taskDefs.add(check);
        taskDefs.add(checkAggregate);

        taskDefs.add(showParsedAst);
        taskDefs.add(showPrettyPrintedText);
        taskDefs.add(showAnalyzedAst);
        taskDefs.add(showScopeGraph);
        taskDefs.add(showDesugaredAst);

        taskDefs.add(compileFile);
        taskDefs.add(compileFileAlt);
        taskDefs.add(compileDirectory);

        return taskDefs;
    }

    @Provides @LanguageScope
    TaskDefs provideTaskDefs(Set<TaskDef<?, ?>> taskDefs) {
        return new MapTaskDefs(taskDefs);
    }

    @Provides @LanguageScope @Named("prototype")
    static Pie providePrototypePie(@Platform Pie pie, TaskDefs taskDefs, ResourceService resourceService) {
        return pie.createChildBuilder().withTaskDefs(taskDefs).withResourceService(resourceService).build();
    }

    @Provides @LanguageScope
    static Pie providePie( @Named("prototype") Pie pie) {
        return pie;
    }

    @Provides @LanguageScope @ElementsIntoSet
    static Set<CommandDef<?>> provideCommandDefsSet(
        TigerShowParsedAstCommand showParsedAstCommand,
        TigerShowDesugaredAstCommand showDesugaredAstCommand,
        TigerShowAnalyzedAstCommand showAnalyzedAstCommand,
        TigerShowScopeGraphCommand showScopeGraphCommand,
        TigerShowPrettyPrintedTextCommand showPrettyPrintedTextCommand,

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

        commandDefs.add(compileFileCommand);
        commandDefs.add(compileDirectoryCommand);
        commandDefs.add(compileFileAltCommand);

        return commandDefs;
    }

    @Provides @LanguageScope @ElementsIntoSet
    static Set<AutoCommandRequest<?>> provideAutoCommandRequestsSet(
        TigerCompileFileCommand tigerCompileFileCommand,
        TigerCompileDirectoryCommand tigerCompileDirectoryCommand
    ) {
        final HashSet<AutoCommandRequest<?>> autoCommandDefs = new HashSet<>();
        autoCommandDefs.add(AutoCommandRequest.of(tigerCompileFileCommand, HierarchicalResourceType.File));
        autoCommandDefs.add(AutoCommandRequest.of(tigerCompileDirectoryCommand, HierarchicalResourceType.Directory));
        return autoCommandDefs;
    }


    @Provides @LanguageScope
    static LanguageInstance provideLanguageInstance(TigerInstance tigerInstance) {
        return tigerInstance;
    }
}
