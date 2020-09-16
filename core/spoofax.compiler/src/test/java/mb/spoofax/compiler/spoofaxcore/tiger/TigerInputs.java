package mb.spoofax.compiler.spoofaxcore.tiger;

import mb.common.util.IOUtil;
import mb.common.util.ListView;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectBuilder;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.adapter.CompleterAdapterCompiler;
import mb.spoofax.compiler.adapter.ConstraintAnalyzerAdapterCompiler;
import mb.spoofax.compiler.adapter.ParserAdapterCompiler;
import mb.spoofax.compiler.adapter.StrategoRuntimeAdapterCompiler;
import mb.spoofax.compiler.adapter.StylerAdapterCompiler;
import mb.spoofax.compiler.adapter.data.ArgProviderRepr;
import mb.spoofax.compiler.adapter.data.AutoCommandRequestRepr;
import mb.spoofax.compiler.adapter.data.CliCommandRepr;
import mb.spoofax.compiler.adapter.data.CliParamRepr;
import mb.spoofax.compiler.adapter.data.CommandActionRepr;
import mb.spoofax.compiler.adapter.data.CommandDefRepr;
import mb.spoofax.compiler.language.CompleterLanguageCompiler;
import mb.spoofax.compiler.language.ConstraintAnalyzerLanguageCompiler;
import mb.spoofax.compiler.language.LanguageProjectBuilder;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import mb.spoofax.compiler.platform.CliProjectCompiler;
import mb.spoofax.compiler.platform.EclipseExternaldepsProjectCompiler;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.platform.IntellijProjectCompiler;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.Shared;
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
    public final ResourcePath rootDirectory;
    public final Shared shared;
    public final LanguageProjectBuilder languageProjectBuilder;
    private LanguageProjectCompiler.@Nullable Input languageProjectCompilerInput = null;
    public final AdapterProjectBuilder adapterProjectBuilder;
    private AdapterProjectCompiler.@Nullable Input adapterProjectCompilerInput = null;

    public TigerInputs(ResourcePath rootDirectory, Shared shared) {
        this.rootDirectory = rootDirectory;
        this.shared = shared;
        this.languageProjectBuilder = new LanguageProjectBuilder();
        setLanguageProjectCompilerInput(rootDirectory, shared, this.languageProjectBuilder);
        this.adapterProjectBuilder = new AdapterProjectBuilder();
        setAdapterProjectCompilerInput(rootDirectory, shared, this.adapterProjectBuilder);
    }

    public TigerInputs(ResourcePath rootDirectory) {
        this(rootDirectory, sharedBuilder().build());
    }

    public static Shared.Builder sharedBuilder() {
        return Shared.builder()
            .name("Tiger")
            .defaultPackageId("mb.tiger")
            ;
    }


    public LanguageProjectCompiler.Input languageProjectCompilerInput() {
        if(languageProjectCompilerInput == null) {
            languageProjectCompilerInput = languageProjectBuilder.build(shared);
        }
        return languageProjectCompilerInput;
    }

    public ParserLanguageCompiler.Input parserLanguageCompilerInput() {
        //noinspection OptionalGetWithoutIsPresent
        return languageProjectCompilerInput().parser().get();
    }

    public StylerLanguageCompiler.Input stylerLanguageCompilerInput() {
        //noinspection OptionalGetWithoutIsPresent
        return languageProjectCompilerInput().styler().get();
    }

    public ConstraintAnalyzerLanguageCompiler.Input constraintAnalyzerLanguageCompilerInput() {
        //noinspection OptionalGetWithoutIsPresent
        return languageProjectCompilerInput().constraintAnalyzer().get();
    }

    public StrategoRuntimeLanguageCompiler.Input strategoRuntimeLanguageCompilerInput() {
        //noinspection OptionalGetWithoutIsPresent
        return languageProjectCompilerInput().strategoRuntime().get();
    }

    public CompleterLanguageCompiler.Input completerLanguageCompilerInput() {
        //noinspection OptionalGetWithoutIsPresent
        return languageProjectCompilerInput().completer().get();
    }


    public AdapterProjectCompiler.Input adapterProjectCompilerInput() {
        if(adapterProjectCompilerInput == null) {
            adapterProjectCompilerInput = adapterProjectBuilder.build(languageProjectCompilerInput());
        }
        return adapterProjectCompilerInput;
    }

    public ParserAdapterCompiler.Input parserAdapterCompilerInput() {
        //noinspection OptionalGetWithoutIsPresent
        return adapterProjectCompilerInput().parser().get();
    }

    public StylerAdapterCompiler.Input stylerAdapterCompilerInput() {
        //noinspection OptionalGetWithoutIsPresent
        return adapterProjectCompilerInput().styler().get();
    }

    public ConstraintAnalyzerAdapterCompiler.Input constraintAnalyzerAdapterCompilerInput() {
        //noinspection OptionalGetWithoutIsPresent
        return adapterProjectCompilerInput().constraintAnalyzer().get();
    }

    public StrategoRuntimeAdapterCompiler.Input strategoRuntimeAdapterCompilerInput() {
        //noinspection OptionalGetWithoutIsPresent
        return adapterProjectCompilerInput().strategoRuntime().get();
    }

    public CompleterAdapterCompiler.Input completerAdapterCompilerInput() {
        //noinspection OptionalGetWithoutIsPresent
        return adapterProjectCompilerInput().completer().get();
    }


    public CliProjectCompiler.Input.Builder cliProjectInput() {
        return CliProjectCompiler.Input.builder()
            .withDefaultProjectFromParentDirectory(rootDirectory, shared)
            .shared(shared)
            .adapterProjectCompilerInput(adapterProjectCompilerInput())
            ;
    }

    public EclipseExternaldepsProjectCompiler.Input.Builder eclipseExternaldepsProjectInput() {
        return EclipseExternaldepsProjectCompiler.Input.builder()
            .withDefaultProjectFromParentDirectory(rootDirectory, shared)
            .shared(shared)
            .adapterProjectCompilerInput(adapterProjectCompilerInput())
            ;
    }

    public EclipseProjectCompiler.Input.Builder eclipseProjectInput() {
        return EclipseProjectCompiler.Input.builder()
            .withDefaultProjectFromParentDirectory(rootDirectory, shared)
            .eclipseExternaldepsDependency(eclipseExternaldepsProjectInput().build().project().asProjectDependency())
            .shared(shared)
            .languageProjectCompilerInput(languageProjectCompilerInput())
            .adapterProjectCompilerInput(adapterProjectCompilerInput())
            ;
    }

    public IntellijProjectCompiler.Input.Builder intellijProjectInput() {
        return IntellijProjectCompiler.Input.builder()
            .withDefaultProjectFromParentDirectory(rootDirectory, shared)
            .shared(shared)
            .adapterProjectCompilerInput(adapterProjectCompilerInput())
            ;
    }


    public void clearBuiltInputs() {
        languageProjectCompilerInput = null;
        adapterProjectCompilerInput = null;
    }


    private static void setLanguageProjectCompilerInput(ResourcePath rootDirectory, Shared shared, LanguageProjectBuilder languageProjectBuilder) {
        languageProjectBuilder.project.withDefaultsFromParentDirectory(rootDirectory, shared);
        languageProjectBuilder.withParser(ParserLanguageCompiler.Input.builder()
            .parseTableRelativePath("target/metaborg/sdf.tbl")
            .startSymbol("Module")
        );
        languageProjectBuilder.withStyler(StylerLanguageCompiler.Input.builder()
            .packedEsvRelativePath("target/metaborg/editor.esv.af")
        );
        languageProjectBuilder.withConstraintAnalyzer(ConstraintAnalyzerLanguageCompiler.Input.builder()
            .enableNaBL2(true)
            .enableStatix(false)
        );
        languageProjectBuilder.withStrategoRuntime(StrategoRuntimeLanguageCompiler.Input.builder()
            .addInteropRegisterersByReflection(
                "org.metaborg.lang.tiger.trans.InteropRegisterer",
                "org.metaborg.lang.tiger.strategies.InteropRegisterer"
            )
        );
        languageProjectBuilder.withCompleter(CompleterLanguageCompiler.Input.builder()
        );
    }

    private static void setAdapterProjectCompilerInput(ResourcePath rootDirectory, Shared shared, AdapterProjectBuilder adapterProjectBuilder) {
        adapterProjectBuilder.project.withDefaultsFromParentDirectory(rootDirectory, shared);
        adapterProjectBuilder.withParser();
        adapterProjectBuilder.withStyler();
        adapterProjectBuilder.withConstraintAnalyzer();
        adapterProjectBuilder.withStrategoRuntime();
        adapterProjectBuilder.withCompleter();
        final AdapterProject adapterProject = adapterProjectBuilder.project.build();

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

        adapterProjectBuilder.adapterProject
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
        ;
    }

    public void copyTaskDefsIntoAdapterProject(ResourceService resourceService) throws IOException {
        final AdapterProject adapterProject = adapterProjectCompilerInput().adapterProject();
        final ResourcePath srcMainJavaDirectory = adapterProject.project().sourceMainJavaDirectory();
        final String taskPackagePath = adapterProject.taskPackagePath();
        final HierarchicalResource taskDirectory = resourceService.getHierarchicalResource(srcMainJavaDirectory.appendRelativePath(taskPackagePath)).ensureDirectoryExists();
        copyResource("TigerShowParsedAstTaskDef.java", taskDirectory);
        copyResource("TigerListDefNames.java", taskDirectory);
        copyResource("TigerListLiteralVals.java", taskDirectory);
        copyResource("TigerCompileFileTaskDef.java", taskDirectory);
        copyResource("TigerAltCompileFileTaskDef.java", taskDirectory);
    }

    private void copyResource(String fileName, HierarchicalResource targetDirectory) throws IOException {
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
}
