package mb.spoofax.compiler.spoofaxcore.tiger;

import mb.common.util.IOUtil;
import mb.common.util.ListView;
import mb.common.util.Preconditions;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.cli.CliCommandRepr;
import mb.spoofax.compiler.cli.CliParamRepr;
import mb.spoofax.compiler.command.ArgProviderRepr;
import mb.spoofax.compiler.command.AutoCommandDefRepr;
import mb.spoofax.compiler.command.CommandDefRepr;
import mb.spoofax.compiler.menu.MenuCommandActionRepr;
import mb.spoofax.compiler.spoofaxcore.AdapterProject;
import mb.spoofax.compiler.spoofaxcore.CliProject;
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzer;
import mb.spoofax.compiler.spoofaxcore.EclipseExternaldepsProject;
import mb.spoofax.compiler.spoofaxcore.EclipseProject;
import mb.spoofax.compiler.spoofaxcore.IntellijProject;
import mb.spoofax.compiler.spoofaxcore.LanguageProject;
import mb.spoofax.compiler.spoofaxcore.Parser;
import mb.spoofax.compiler.spoofaxcore.RootProject;
import mb.spoofax.compiler.spoofaxcore.Shared;
import mb.spoofax.compiler.spoofaxcore.StrategoRuntime;
import mb.spoofax.compiler.spoofaxcore.Styler;
import mb.spoofax.compiler.util.Conversion;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.StringUtil;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandExecutionType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

public class TigerInputs {
    /// Shared input

    public static Shared.Builder sharedBuilder(ResourcePath baseDirectory) {
        return Shared.builder()
            .name("Tiger")
            .basePackageId("mb.tiger")
            .baseDirectory(baseDirectory)
            /// Metaborg log
            .logApiDep(fromSystemProperty("log.api:classpath"))
            .logBackendSLF4JDep(fromSystemProperty("log.backend.slf4j:classpath"))
            /// Metaborg resource
            .resourceDep(fromSystemProperty("resource:classpath"))
            /// PIE
            .pieApiDep(fromSystemProperty("pie.api:classpath"))
            .pieRuntimeDep(fromSystemProperty("pie.runtime:classpath"))
            .pieDaggerDep(fromSystemProperty("pie.dagger:classpath"))
            /// Spoofax-PIE
            .commonDep(fromSystemProperty("common:classpath"))
            .jsglrCommonDep(fromSystemProperty("jsglr.common:classpath"))
            .jsglr1CommonDep(fromSystemProperty("jsglr1.common:classpath"))
            .jsglr2CommonDep(fromSystemProperty("jsglr2.common:classpath"))
            .esvCommonDep(fromSystemProperty("esv.common:classpath"))
            .strategoCommonDep(fromSystemProperty("stratego.common:classpath"))
            .constraintCommonDep(fromSystemProperty("constraint.common:classpath"))
            .nabl2CommonDep(fromSystemProperty("nabl2.common:classpath"))
            .statixCommonDep(fromSystemProperty("statix.common:classpath"))
            .spoofaxCompilerInterfacesDep(fromSystemProperty("spoofax.compiler.interfaces:classpath"))
            .spoofaxCoreDep(fromSystemProperty("spoofax.core:classpath"))
            .spoofaxCliDep(fromSystemProperty("spoofax.cli:classpath"))
            .spoofaxEclipseDep(fromSystemProperty("spoofax.eclipse:classpath"))
            .spoofaxEclipseExternaldepsDep(fromSystemProperty("spoofax.eclipse.externaldeps:classpath"))
            .spoofaxIntellijDep(fromSystemProperty("spoofax.intellij:classpath"))
            ;
    }

    private static GradleDependency fromSystemProperty(String key) {
        return GradleDependency.files(Preconditions.checkNotNull(System.getProperty(key)));
    }

    public static Shared shared(ResourcePath baseDirectory) {
        return sharedBuilder(baseDirectory).build();
    }


    /// Parser compiler input

    public static Parser.Input.Builder parserBuilder(Shared shared) {
        return Parser.Input.builder()
            .startSymbol("Module")
            .shared(shared)
            ;
    }

    public static Parser.Input parser(Shared shared) {
        return parserBuilder(shared).build();
    }


    /// Styler compiler input

    public static Styler.Input.Builder stylerBuilder(Shared shared) {
        return Styler.Input.builder()
            .parser(parser(shared))
            .shared(shared)
            ;
    }

    public static Styler.Input styler(Shared shared) {
        return stylerBuilder(shared).build();
    }


    /// Stratego runtime builder compiler input

    public static StrategoRuntime.Input.Builder strategoRuntimeBuilder(Shared shared) {
        return StrategoRuntime.Input.builder()
            .shared(shared)
            .addInteropRegisterersByReflection("org.metaborg.lang.tiger.trans.InteropRegisterer", "org.metaborg.lang.tiger.strategies.InteropRegisterer")
            .addNaBL2Primitives(true)
            .addStatixPrimitives(false)
            .copyJavaStrategyClasses(true)
            ;
    }

    public static StrategoRuntime.Input strategoRuntime(Shared shared) {
        return strategoRuntimeBuilder(shared).build();
    }


    /// Constraint analyzer compiler input

    public static ConstraintAnalyzer.Input.Builder constraintAnalyzerBuilder(Shared shared) {
        return ConstraintAnalyzer.Input.builder()
            .shared(shared)
            .parse(parser(shared))
            ;
    }

    public static ConstraintAnalyzer.Input constraintAnalyzer(Shared shared) {
        return constraintAnalyzerBuilder(shared).build();
    }


    /// Language project compiler input

    public static LanguageProject.Input.Builder languageProjectBuilder(Shared shared) {
        return LanguageProject.Input.builder()
            .shared(shared)
            .parser(parser(shared))
            .styler(styler(shared))
            .strategoRuntime(strategoRuntime(shared))
            .constraintAnalyzer(constraintAnalyzer(shared))
            .languageSpecificationDependency(GradleDependency.files(Preconditions.checkNotNull(System.getProperty("org.metaborg.lang.tiger:classpath"))))
            ;
    }

    public static LanguageProject.Input languageProject(Shared shared) {
        return languageProjectBuilder(shared).build();
    }


    /// Adapter project compiler input

    public static AdapterProject.Input.Builder adapterProjectBuilder(Shared shared) {
        final TypeInfo showParsedAstTaskDef = TypeInfo.of(shared.adapterTaskPackage(), "TigerShowParsedAstTaskDef");
        final TypeInfo listDefNamesTaskDef = TypeInfo.of(shared.adapterTaskPackage(), "TigerListDefNames");
        final TypeInfo listLiteralValsTaskDef = TypeInfo.of(shared.adapterTaskPackage(), "TigerListLiteralVals");
        final TypeInfo tigerCompileFileTaskDef = TypeInfo.of(shared.adapterTaskPackage(), "TigerCompileFileTaskDef");
        final TypeInfo tigerAltCompileFileTaskDef = TypeInfo.of(shared.adapterTaskPackage(), "TigerAltCompileFileTaskDef");

        final CommandDefRepr tigerShowParsedAst = CommandDefRepr.builder()
            .type(shared.adapterCommandPackage(), "TigerShowParsedAst")
            .taskDefType(showParsedAstTaskDef)
            .argType(shared.adapterTaskPackage(), "TigerShowParsedAstTaskDef.Args")
            .displayName("Show parsed AST")
            .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
            .addRequiredContextTypes(CommandContextType.Resource)
            .addParams("resource", TypeInfo.of("mb.resource", "ResourceKey"), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context()))
            .addParams("region", TypeInfo.of("mb.common.region", "Region"), false, Optional.empty(), Collections.singletonList(ArgProviderRepr.context()))
            .build();

        final CommandDefRepr tigerCompileFile = CommandDefRepr.builder()
            .type(TypeInfo.of(shared.adapterCommandPackage(), "TigerCompileFile"))
            .taskDefType(tigerCompileFileTaskDef)
            .argType(shared.adapterTaskPackage(), "TigerCompileFileTaskDef.Args")
            .displayName("'Compile' file (list literals)")
            .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous, CommandExecutionType.AutomaticContinuous)
            .addRequiredContextTypes(CommandContextType.File)
            .addParams("file", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context()))
            .build();

        final CommandDefRepr tigerAltCompileFile = CommandDefRepr.builder()
            .type(TypeInfo.of(shared.adapterCommandPackage(), "TigerAltCompileFile"))
            .taskDefType(tigerAltCompileFileTaskDef)
            .argType(shared.adapterTaskPackage(), "TigerAltCompileFileTaskDef.Args")
            .displayName("'Alternative compile' file")
            .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous, CommandExecutionType.AutomaticContinuous)
            .addRequiredContextTypes(CommandContextType.File)
            .addParams("file", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context()))
            .addParams("listDefNames", TypeInfo.ofBoolean(), false, Optional.empty(), Collections.singletonList(ArgProviderRepr.value("true")))
            .addParams("base64Encode", TypeInfo.ofBoolean(), false, Optional.empty(), Collections.singletonList(ArgProviderRepr.value("false")))
            .addParams("compiledFileNameSuffix", TypeInfo.ofString(), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.value(StringUtil.doubleQuote("defnames.aterm"))))
            .build();

        return AdapterProject.Input.builder()
            .shared(shared)
            .parser(parser(shared))
            .styler(styler(shared))
            .strategoRuntime(strategoRuntime(shared))
            .constraintAnalyzer(constraintAnalyzer(shared))
            .addTaskDefs(
                showParsedAstTaskDef,
                listDefNamesTaskDef,
                listLiteralValsTaskDef,
                tigerCompileFileTaskDef,
                tigerAltCompileFileTaskDef
            )
            .addCommandDefs(
                tigerShowParsedAst,
                tigerCompileFile,
                tigerAltCompileFile
            )
            .addAutoCommandDefs(AutoCommandDefRepr.builder()
                .commandDef(tigerCompileFile.type())
                .build()
            )
            .addAutoCommandDefs(AutoCommandDefRepr.builder()
                .commandDef(tigerAltCompileFile.type())
                .putRawArgs("base64Encode", "true")
                .build()
            )
            .cliCommand(CliCommandRepr.builder()
                .name("tiger")
                .description("Tiger language command-line interface")
                .addSubCommands(
                    CliCommandRepr.builder()
                        .name("parse")
                        .description("Parses Tiger sources and shows the parsed AST")
                        .commandDefType(tigerShowParsedAst.type())
                        .addParams(
                            CliParamRepr.positional("resource", 0, "FILE", "Source file to parse", null),
                            CliParamRepr.option("region", ListView.of("-r", "--region"), false, null, "Region in source file to parse", null)
                        )
                        .build()
                )
                .build()
            )
            .addEditorContextMenuItems(
                MenuCommandActionRepr.builder()
                    .commandDefType(tigerShowParsedAst.type())
                    .executionType(CommandExecutionType.ManualContinuous)
                    .build()
            )
            ;
    }

    public static void copyTaskDefsIntoAdapterProject(AdapterProject.Input input, ResourceService resourceService) throws IOException {
        final ResourcePath srcMainJavaDirectory = input.shared().adapterProject().sourceMainJavaDirectory();
        final String taskPackagePath = Conversion.packageIdToPath(input.shared().adapterTaskPackage());
        final HierarchicalResource taskDirectory = resourceService.getHierarchicalResource(srcMainJavaDirectory.appendRelativePath(taskPackagePath)).ensureDirectoryExists();
        copyResource("TigerShowParsedAstTaskDef.java", taskDirectory);
        copyResource("TigerListDefNames.java", taskDirectory);
        copyResource("TigerListLiteralVals.java", taskDirectory);
        copyResource("TigerCompileFileTaskDef.java", taskDirectory);
        copyResource("TigerAltCompileFileTaskDef.java", taskDirectory);
    }

    private static void copyResource(String fileName, HierarchicalResource targetDirectory) throws IOException {
        try(final @Nullable InputStream inputStream = TigerInputs.class.getResourceAsStream(fileName)) {
            if(inputStream == null) {
                throw new IllegalStateException("Cannot get input stream for resource '" + fileName + "'");
            }
            IOUtil.copy(inputStream, targetDirectory.appendSegment(fileName).openWrite());
        }
    }


    /// CLI project compiler input

    public static CliProject.Input.Builder cliProjectBuilder(Shared shared, AdapterProject.Input adapterProject) {
        return CliProject.Input.builder()
            .shared(shared)
            .adapterProject(adapterProject)
            ;
    }


    /// Eclipse externaldeps project compiler input

    public static EclipseExternaldepsProject.Input.Builder eclipseExternaldepsProjectBuilder(Shared shared) {
        return EclipseExternaldepsProject.Input.builder()
            .shared(shared)
            ;
    }


    /// Eclipse project compiler input

    public static EclipseProject.Input.Builder eclipseProjectBuilder(Shared shared, AdapterProject.Input adapterProject) {
        return EclipseProject.Input.builder()
            .shared(shared)
            .adapterProject(adapterProject)
            ;
    }


    /// Intellij project compiler input

    public static IntellijProject.Input.Builder intellijProjectBuilder(Shared shared, AdapterProject.Input adapterProject) {
        return IntellijProject.Input.builder()
            .shared(shared)
            .adapterProject(adapterProject)
            ;
    }


    /// Root project compiler input

    public static RootProject.Input.Builder rootProjectBuilder(Shared shared) {
        return RootProject.Input.builder()
            .shared(shared)
            ;
    }

    public static RootProject.Input rootProject(Shared shared) {
        return rootProjectBuilder(shared).build();
    }
}
