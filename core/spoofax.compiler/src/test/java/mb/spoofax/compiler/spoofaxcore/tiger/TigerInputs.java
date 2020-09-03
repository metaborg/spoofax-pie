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
import mb.spoofax.compiler.command.AutoCommandRequestRepr;
import mb.spoofax.compiler.command.CommandDefRepr;
import mb.spoofax.compiler.menu.CommandActionRepr;
import mb.spoofax.compiler.spoofaxcore.AdapterProject;
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.ClassloaderResourcesCompiler;
import mb.spoofax.compiler.spoofaxcore.CliProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.CompleterCompiler;
import mb.spoofax.compiler.spoofaxcore.ConstraintAnalyzerCompiler;
import mb.spoofax.compiler.spoofaxcore.EclipseExternaldepsProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.EclipseProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.IntellijProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.LanguageProject;
import mb.spoofax.compiler.spoofaxcore.LanguageProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.ParserCompiler;
import mb.spoofax.compiler.spoofaxcore.RootProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.Shared;
import mb.spoofax.compiler.spoofaxcore.StrategoRuntimeCompiler;
import mb.spoofax.compiler.spoofaxcore.StylerCompiler;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.StringUtil;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandExecutionType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Optional;

public class TigerInputs {
    /// Shared

    public static Shared.Builder shared(ResourcePath baseDirectory) {
        return Shared.builder()
            .name("Tiger")
            .baseDirectory(baseDirectory)
            .defaultBasePackageId("mb.tiger")
            ;
    }

    private static GradleDependency fromSystemProperty(String key) {
        return GradleDependency.files(Preconditions.checkNotNull(System.getProperty(key)));
    }

    /// Main projects

    public static LanguageProject.Builder languageProject(Shared shared) {
        return LanguageProject.builder().shared(shared);
    }

    public static AdapterProject.Builder adapterProject(Shared shared) {
        return AdapterProject.builder().shared(shared);
    }

    /// Classloader resources compiler

    public static ClassloaderResourcesCompiler.LanguageProjectInput.Builder classloaderResourcesLanguageProjectInput(Shared shared, LanguageProject languageProject) {
        return ClassloaderResourcesCompiler.LanguageProjectInput.builder()
            .shared(shared)
            .languageProject(languageProject)
            ;
    }

    public static ClassloaderResourcesCompiler.AdapterProjectInput.Builder classloaderResourcesAdapterProjectInput(Shared shared, LanguageProject languageProject) {
        return ClassloaderResourcesCompiler.AdapterProjectInput.builder()
            .languageProjectInput(classloaderResourcesLanguageProjectInput(shared, languageProject).build())
            ;
    }

    /// Parser compiler

    public static ParserCompiler.LanguageProjectInput.Builder parserLanguageProjectInput(Shared shared, LanguageProject languageProject) {
        return ParserCompiler.LanguageProjectInput.builder()
            .startSymbol("Module")
            .shared(shared)
            .languageProject(languageProject)
            ;
    }

    public static ParserCompiler.AdapterProjectInput.Builder parserAdapterProjectInput(Shared shared, LanguageProject languageProject, AdapterProject adapterProject) {
        return ParserCompiler.AdapterProjectInput.builder()
            .shared(shared)
            .adapterProject(adapterProject)
            .languageProjectInput(parserLanguageProjectInput(shared, languageProject).build())
            ;
    }

    /// Styler compiler

    public static StylerCompiler.LanguageProjectInput.Builder stylerLanguageProjectInput(Shared shared, LanguageProject languageProject) {
        return StylerCompiler.LanguageProjectInput.builder()
            .shared(shared)
            .languageProject(languageProject)
            ;
    }

    public static StylerCompiler.AdapterProjectInput.Builder stylerAdapterProjectInput(Shared shared, LanguageProject languageProject, AdapterProject adapterProject) {
        return StylerCompiler.AdapterProjectInput.builder()
            .shared(shared)
            .adapterProject(adapterProject)
            .languageProjectInput(stylerLanguageProjectInput(shared, languageProject).build())
            ;
    }

    /// Completer compiler

    public static CompleterCompiler.LanguageProjectInput.Builder completerLanguageProjectInput(Shared shared, LanguageProject languageProject) {
        return CompleterCompiler.LanguageProjectInput.builder()
            .shared(shared)
            .languageProject(languageProject)
            ;
    }

    public static CompleterCompiler.AdapterProjectInput.Builder completerAdapterProjectInput(Shared shared, LanguageProject languageProject, AdapterProject adapterProject) {
        return CompleterCompiler.AdapterProjectInput.builder()
            .shared(shared)
            .adapterProject(adapterProject)
            .languageProjectInput(completerLanguageProjectInput(shared, languageProject).build())
            ;
    }

    /// Stratego runtime compiler

    public static StrategoRuntimeCompiler.LanguageProjectInput.Builder strategoRuntimeLanguageProjectInput(Shared shared, LanguageProject languageProject) {
        return StrategoRuntimeCompiler.LanguageProjectInput.builder()
            .addInteropRegisterersByReflection(
                "org.metaborg.lang.tiger.trans.InteropRegisterer",
                "org.metaborg.lang.tiger.strategies.InteropRegisterer"
            )
            .enableNaBL2(true)
            .enableStatix(false)
            .copyClasses(true)
            .shared(shared)
            .languageProject(languageProject)
            ;
    }

    public static StrategoRuntimeCompiler.AdapterProjectInput.Builder strategoRuntimeAdapterProjectInput(Shared shared, LanguageProject languageProject, AdapterProject adapterProject) {
        return StrategoRuntimeCompiler.AdapterProjectInput.builder()
            .languageProjectInput(strategoRuntimeLanguageProjectInput(shared, languageProject).build())
            ;
    }

    /// Constraint analyzer compiler

    public static ConstraintAnalyzerCompiler.LanguageProjectInput.Builder constraintAnalyzerLanguageProjectInput(Shared shared, LanguageProject languageProject) {
        return ConstraintAnalyzerCompiler.LanguageProjectInput.builder()
            .shared(shared)
            .languageProject(languageProject)
            ;
    }

    public static ConstraintAnalyzerCompiler.AdapterProjectInput.Builder constraintAnalyzerAdapterProjectInput(Shared shared, LanguageProject languageProject, AdapterProject adapterProject) {
        return ConstraintAnalyzerCompiler.AdapterProjectInput.builder()
            .shared(shared)
            .adapterProject(adapterProject)
            .languageProjectInput(constraintAnalyzerLanguageProjectInput(shared, languageProject).build())
            ;
    }

    /// Language project compiler

    public static LanguageProjectCompiler.Input.Builder languageProjectInput(Shared shared, LanguageProject languageProject) {
        return LanguageProjectCompiler.Input.builder()
            .languageProject(languageProject)
            .classloaderResources(classloaderResourcesLanguageProjectInput(shared, languageProject).build())
            .parser(parserLanguageProjectInput(shared, languageProject).build())
            .styler(stylerLanguageProjectInput(shared, languageProject).build())
            .completer(completerLanguageProjectInput(shared, languageProject).build())
            .strategoRuntime(strategoRuntimeLanguageProjectInput(shared, languageProject).build())
            .constraintAnalyzer(constraintAnalyzerLanguageProjectInput(shared, languageProject).build())
            .languageSpecificationDependency(GradleDependency.project(":tiger"))
            .shared(shared)
            ;
    }

    /// Adapter project compiler

    public static AdapterProjectCompiler.Input.Builder adapterProjectInput(Shared shared, LanguageProject languageProject, AdapterProject adapterProject) {
        final TypeInfo showParsedAstTaskDef = TypeInfo.of(adapterProject.taskPackageId(), "TigerShowParsedAstTaskDef");
        final TypeInfo listDefNamesTaskDef = TypeInfo.of(adapterProject.taskPackageId(), "TigerListDefNames");
        final TypeInfo listLiteralValsTaskDef = TypeInfo.of(adapterProject.taskPackageId(), "TigerListLiteralVals");
        final TypeInfo tigerCompileFileTaskDef = TypeInfo.of(adapterProject.taskPackageId(), "TigerCompileFileTaskDef");
        final TypeInfo tigerAltCompileFileTaskDef = TypeInfo.of(adapterProject.taskPackageId(), "TigerAltCompileFileTaskDef");

        final CommandDefRepr tigerShowParsedAst = CommandDefRepr.builder()
            .type(adapterProject.commandPackageId(), "TigerShowParsedAst")
            .taskDefType(showParsedAstTaskDef)
            .argType(adapterProject.taskPackageId(), "TigerShowParsedAstTaskDef.Args")
            .displayName("Show parsed AST")
            .description("Shows the parsed Abstract Syntax Tree of the program.")
            .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous)
            .addParams("resource", TypeInfo.of("mb.resource", "ResourceKey"), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.ResourceKey)))
            .addParams("region", TypeInfo.of("mb.common.region", "Region"), false, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.Region)))
            .build();

        final CommandDefRepr tigerCompileFile = CommandDefRepr.builder()
            .type(TypeInfo.of(adapterProject.commandPackageId(), "TigerCompileFile"))
            .taskDefType(tigerCompileFileTaskDef)
            .argType(adapterProject.taskPackageId(), "TigerCompileFileTaskDef.Args")
            .displayName("'Compile' file (list literals)")
            .description("")
            .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous, CommandExecutionType.AutomaticContinuous)
            .addParams("file", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.File)))
            .build();

        final CommandDefRepr tigerAltCompileFile = CommandDefRepr.builder()
            .type(TypeInfo.of(adapterProject.commandPackageId(), "TigerAltCompileFile"))
            .taskDefType(tigerAltCompileFileTaskDef)
            .argType(adapterProject.taskPackageId(), "TigerAltCompileFileTaskDef.Args")
            .displayName("'Alternative compile' file")
            .description("")
            .addSupportedExecutionTypes(CommandExecutionType.ManualOnce, CommandExecutionType.ManualContinuous, CommandExecutionType.AutomaticContinuous)
            .addParams("file", TypeInfo.of("mb.resource.hierarchical", "ResourcePath"), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.File)))
            .addParams("listDefNames", TypeInfo.ofBoolean(), false, Optional.empty(), Collections.singletonList(ArgProviderRepr.value("true")))
            .addParams("base64Encode", TypeInfo.ofBoolean(), false, Optional.empty(), Collections.singletonList(ArgProviderRepr.value("false")))
            .addParams("compiledFileNameSuffix", TypeInfo.ofString(), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.value(StringUtil.doubleQuote("defnames.aterm"))))
            .build();

        return AdapterProjectCompiler.Input.builder()
            .adapterProject(adapterProject)
            .classloaderResources(classloaderResourcesAdapterProjectInput(shared, languageProject).build())
            .parser(parserAdapterProjectInput(shared, languageProject, adapterProject).build())
            .styler(stylerAdapterProjectInput(shared, languageProject, adapterProject).build())
            .completer(completerAdapterProjectInput(shared, languageProject, adapterProject).build())
            .strategoRuntime(strategoRuntimeAdapterProjectInput(shared, languageProject, adapterProject).build())
            .constraintAnalyzer(constraintAnalyzerAdapterProjectInput(shared, languageProject, adapterProject).build())
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
            .addAutoCommandDefs(AutoCommandRequestRepr.builder()
                .commandDef(tigerCompileFile.type())
                .build()
            )
            .addAutoCommandDefs(AutoCommandRequestRepr.builder()
                .commandDef(tigerAltCompileFile.type())
                .putInitialArgs("base64Encode", "true")
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
                CommandActionRepr.builder().manualOnce(tigerShowParsedAst).buildItem()
            )
            .shared(shared)
            ;
    }

    public static void copyTaskDefsIntoAdapterProject(AdapterProjectCompiler.Input input, ResourceService resourceService) throws IOException {
        final ResourcePath srcMainJavaDirectory = input.adapterProject().project().sourceMainJavaDirectory();
        final String taskPackagePath = input.adapterProject().taskPackagePath();
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
            try(final OutputStream outputStream = targetDirectory.appendSegment(fileName).openWrite()) {
                IOUtil.copy(inputStream, outputStream);
                outputStream.flush();
            }
        }
    }

    /// CLI project compiler

    public static CliProjectCompiler.Input.Builder cliProjectInput(Shared shared, AdapterProjectCompiler.Input adapterProjectCompilerInput) {
        return CliProjectCompiler.Input.builder()
            .shared(shared)
            .adapterProjectCompilerInput(adapterProjectCompilerInput)
            ;
    }

    /// Eclipse externaldeps project compiler

    public static EclipseExternaldepsProjectCompiler.Input.Builder eclipseExternaldepsProjectInput(
        Shared shared,
        LanguageProject languageProject,
        AdapterProject adapterProject
    ) {
        return EclipseExternaldepsProjectCompiler.Input.builder()
            .shared(shared)
            .adapterProjectCompilerInput(adapterProjectInput(shared, languageProject, adapterProject)
                .languageProjectDependency(GradleDependency.project(":" + languageProject.project().coordinate().artifactId()))
                .build())
            ;
    }

    /// Eclipse project compiler

    public static EclipseProjectCompiler.Input.Builder eclipseProjectInput(
        Shared shared,
        LanguageProjectCompiler.Input languageProjectInput,
        AdapterProjectCompiler.Input adapterProjectCompilerInput
    ) {
        return EclipseProjectCompiler.Input.builder()
            .shared(shared)
            .languageProjectCompilerInput(languageProjectInput)
            .adapterProjectCompilerInput(adapterProjectCompilerInput)
            ;
    }

    /// Intellij project compiler

    public static IntellijProjectCompiler.Input.Builder intellijProjectInput(Shared shared, AdapterProjectCompiler.Input adapterProjectCompilerInput) {
        return IntellijProjectCompiler.Input.builder()
            .shared(shared)
            .adapterProjectCompilerInput(adapterProjectCompilerInput)
            ;
    }

    /// Root project compiler

    public static RootProjectCompiler.Input.Builder rootProjectInput(Shared shared) {
        return RootProjectCompiler.Input.builder()
            .shared(shared)
            ;
    }
}
