package mb.spoofax.compiler.spoofaxcore.tiger;

import mb.common.option.Option;
import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.compiler.adapter.AdapterProject;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.adapter.AdapterProjectCompilerInputBuilder;
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
import mb.spoofax.compiler.language.LanguageProject;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompilerInputBuilder;
import mb.spoofax.compiler.language.ParserLanguageCompiler;
import mb.spoofax.compiler.language.StrategoRuntimeLanguageCompiler;
import mb.spoofax.compiler.language.StylerLanguageCompiler;
import mb.spoofax.compiler.platform.CliProjectCompiler;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.platform.IntellijProjectCompiler;
import mb.spoofax.compiler.util.GradleDependency;
import mb.spoofax.compiler.util.Shared;
import mb.spoofax.compiler.util.StringUtil;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandExecutionType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.Optional;

public class TigerInputs {
    public final ResourcePath rootDirectory;
    public final Shared shared;

    public final LanguageProject languageProject;
    public final boolean adapterProjectAsSeparateProject;
    public final AdapterProject adapterProject;

    public final LanguageProjectCompilerInputBuilder languageProjectCompilerInputBuilder;
    private LanguageProjectCompiler.@Nullable Input languageProjectCompilerInput = null;

    public final AdapterProjectCompilerInputBuilder adapterProjectCompilerInputBuilder;
    private AdapterProjectCompiler.@Nullable Input adapterProjectCompilerInput = null;

    public TigerInputs(ResourcePath rootDirectory, Shared shared, boolean adapterProjectAsSeparateProject) {
        this.rootDirectory = rootDirectory;
        this.shared = shared;

        this.languageProject = LanguageProject.builder().withDefaultsFromParentDirectory(rootDirectory, shared).build();
        this.languageProjectCompilerInputBuilder = new LanguageProjectCompilerInputBuilder();
        setLanguageProjectCompilerInput(rootDirectory, shared, this.languageProjectCompilerInputBuilder);

        this.adapterProjectAsSeparateProject = adapterProjectAsSeparateProject;

        this.adapterProject = AdapterProject.builder().withDefaultsFromParentDirectory(rootDirectory, shared).build();
        this.adapterProjectCompilerInputBuilder = new AdapterProjectCompilerInputBuilder();
        setAdapterProjectCompilerInput(rootDirectory, shared, this.adapterProjectCompilerInputBuilder);
    }

    public TigerInputs(ResourcePath rootDirectory, boolean adapterProjectAsSeparateProject) {
        this(rootDirectory, sharedBuilder().build(), adapterProjectAsSeparateProject);
    }

    public static Shared.Builder sharedBuilder() {
        return Shared.builder()
            .name("Tiger")
            .defaultPackageId("mb.tiger")
            ;
    }


    public LanguageProjectCompiler.Input languageProjectCompilerInput() {
        if(languageProjectCompilerInput == null) {
            languageProjectCompilerInput = languageProjectCompilerInputBuilder.build(shared, languageProject);
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
            final LanguageProjectCompiler.Input languageProjectCompilerInput = languageProjectCompilerInput();
            final Option<GradleDependency> languageProjectDependency;
            if(adapterProjectAsSeparateProject) {
                languageProjectDependency = Option.ofSome(languageProjectCompilerInput.languageProject().project().asProjectDependency());
            } else {
                languageProjectDependency = Option.ofNone();
            }
            adapterProjectCompilerInput = adapterProjectCompilerInputBuilder.build(languageProjectCompilerInput, languageProjectDependency, adapterProject);
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

    public EclipseProjectCompiler.Input.Builder eclipseProjectInput() {
        return EclipseProjectCompiler.Input.builder()
            .withDefaultProjectFromParentDirectory(rootDirectory, shared)
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


    private void setLanguageProjectCompilerInput(ResourcePath rootDirectory, Shared shared, LanguageProjectCompilerInputBuilder languageProjectCompilerInputBuilder) {
        languageProjectCompilerInputBuilder.withParser()
            .parseTableAtermFileRelativePath("target/metaborg/sdf.tbl")
            .parseTablePersistedFileRelativePath("target/metaborg/table.bin")
            .startSymbol("Module");
        languageProjectCompilerInputBuilder.withStyler()
            .packedEsvRelativePath("target/metaborg/editor.esv.af");
        languageProjectCompilerInputBuilder.withConstraintAnalyzer()
            .enableNaBL2(true)
            .enableStatix(false);
        languageProjectCompilerInputBuilder.withStrategoRuntime()
            .addInteropRegisterersByReflection(
                "org.metaborg.lang.tiger.trans.InteropRegisterer",
                "org.metaborg.lang.tiger.strategies.InteropRegisterer"
            );
        languageProjectCompilerInputBuilder.withCompleter();
    }

    private void setAdapterProjectCompilerInput(ResourcePath rootDirectory, Shared shared, AdapterProjectCompilerInputBuilder adapterProjectCompilerInputBuilder) {
        adapterProjectCompilerInputBuilder.withParser();
        adapterProjectCompilerInputBuilder.withStyler();
        adapterProjectCompilerInputBuilder.withConstraintAnalyzer();
        adapterProjectCompilerInputBuilder.withStrategoRuntime();
        adapterProjectCompilerInputBuilder.withCompleter();

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
            .addParams("resource", TypeInfo.of("mb.resource", "ResourceKey"), true, Optional.empty(), Collections.singletonList(ArgProviderRepr.context(CommandContextType.ReadableResource)))
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

        adapterProjectCompilerInputBuilder.project
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
}
