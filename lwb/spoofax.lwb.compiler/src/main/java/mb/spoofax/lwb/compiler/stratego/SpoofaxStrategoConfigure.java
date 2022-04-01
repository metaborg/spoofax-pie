package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.metalang.CfgStrategoConfig;
import mb.cfg.metalang.CfgStrategoSource;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.gpp.GppInfo;
import mb.gpp.GppUtil;
import mb.libspoofax2.LibSpoofax2ClassLoaderResources;
import mb.libspoofax2.LibSpoofax2ResourceExports;
import mb.libstatix.LibStatixClassLoaderResources;
import mb.libstatix.LibStatixResourceExports;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.STask;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.Sdf3ToCompletionRuntime;
import mb.sdf3.task.Sdf3ToPrettyPrinter;
import mb.sdf3.task.Sdf3ToSignature;
import mb.sdf3.task.spec.Sdf3ParseTableToParenthesizer;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spec.Sdf3SpecToParseTable;
import mb.sdf3_ext_statix.task.Sdf3ExtStatixGenerateStratego;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.lwb.compiler.definition.ResolveIncludesException;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3ConfigureException;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3GenerationUtil;
import mb.str.config.StrategoCompileConfig;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.ModuleIdentifier;
import mb.stratego.build.strincr.Stratego2LibInfo;
import mb.strategolib.StrategoLibInfo;
import mb.strategolib.StrategoLibUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Configure task for Stratego in the context of the Spoofax LWB compiler.
 */
public class SpoofaxStrategoConfigure implements TaskDef<ResourcePath, Result<Option<StrategoCompileConfig>, SpoofaxStrategoConfigureException>> {
    private final TemplateWriter completionTemplate;
    private final TemplateWriter ppTemplate;

    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final SpoofaxStrategoResolveIncludes resolveIncludes;
    private final UnarchiveFromJar unarchiveFromJar;

    private final StrategoLibUtil strategoLibUtil;
    private final GppUtil gppUtil;
    private final SpoofaxStrategoGenerationUtil spoofaxStrategoGenerationUtil;

    private final SpoofaxSdf3GenerationUtil spoofaxSdf3GenerationUtil;
    private final Sdf3SpecToParseTable sdf3ToParseTable;
    private final Sdf3ToSignature sdf3ToSignature;
    private final Sdf3ToPrettyPrinter sdf3ToPrettyPrinter;
    private final Sdf3ParseTableToParenthesizer sdf3ToParenthesizer;
    private final Sdf3ToCompletionRuntime sdf3ToCompletionRuntime;
    private final Sdf3ExtStatixGenerateStratego sdf3ExtStatixGenerateStratego;


    @Inject public SpoofaxStrategoConfigure(
        TemplateCompiler templateCompiler,

        CfgRootDirectoryToObject cfgRootDirectoryToObject,

        SpoofaxStrategoResolveIncludes resolveIncludes,
        UnarchiveFromJar unarchiveFromJar,

        StrategoLibUtil strategoLibUtil,
        GppUtil gppUtil,
        SpoofaxStrategoGenerationUtil spoofaxStrategoGenerationUtil,

        SpoofaxSdf3GenerationUtil spoofaxSdf3GenerationUtil,
        Sdf3SpecToParseTable sdf3ToParseTable,
        Sdf3ToSignature sdf3ToSignature,
        Sdf3ToPrettyPrinter sdf3ToPrettyPrinter,
        Sdf3ParseTableToParenthesizer sdf3ToParenthesizer,
        Sdf3ToCompletionRuntime sdf3ToCompletionRuntime,
        Sdf3ExtStatixGenerateStratego sdf3ExtStatixGenerateStratego
    ) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.completionTemplate = templateCompiler.getOrCompileToWriter("completion.str2.mustache");
        this.ppTemplate = templateCompiler.getOrCompileToWriter("pp.str2.mustache");

        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;

        this.unarchiveFromJar = unarchiveFromJar;
        this.resolveIncludes = resolveIncludes;

        this.strategoLibUtil = strategoLibUtil;
        this.gppUtil = gppUtil;
        this.spoofaxStrategoGenerationUtil = spoofaxStrategoGenerationUtil;

        this.spoofaxSdf3GenerationUtil = spoofaxSdf3GenerationUtil;
        this.sdf3ToParseTable = sdf3ToParseTable;
        this.sdf3ToSignature = sdf3ToSignature;
        this.sdf3ToPrettyPrinter = sdf3ToPrettyPrinter;
        this.sdf3ToParenthesizer = sdf3ToParenthesizer;
        this.sdf3ToCompletionRuntime = sdf3ToCompletionRuntime;
        this.sdf3ExtStatixGenerateStratego = sdf3ExtStatixGenerateStratego;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Option<StrategoCompileConfig>, SpoofaxStrategoConfigureException> exec(ExecContext context, ResourcePath rootDirectory) throws Exception {
        return context.requireMapping(cfgRootDirectoryToObject, rootDirectory, SpoofaxStrategoConfigMapper.instance)
            .mapErr(SpoofaxStrategoConfigureException::getLanguageCompilerConfigurationFail)
            .<Option<StrategoCompileConfig>, Exception>flatMapThrowing(o -> Result.transpose(o.mapThrowing(c -> toStrategoConfig(context, rootDirectory, c, c.source().getFiles()))));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public Result<StrategoCompileConfig, SpoofaxStrategoConfigureException> toStrategoConfig(
        ExecContext context,
        ResourcePath rootDirectory,
        CfgStrategoConfig cfgStrategoConfig,
        CfgStrategoSource.Files sourceFiles
    ) throws IOException, InterruptedException {
        // Check main source directory, main file, and include directories.
        final HierarchicalResource mainSourceDirectory = context.require(sourceFiles.mainSourceDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainSourceDirectory.exists() || !mainSourceDirectory.isDirectory()) {
            return Result.ofErr(SpoofaxStrategoConfigureException.mainSourceDirectoryFail(mainSourceDirectory.getPath()));
        }
        final HierarchicalResource mainFile = context.require(sourceFiles.mainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(SpoofaxStrategoConfigureException.mainFileFail(mainFile.getPath()));
        }
        for(ResourcePath includeDirectoryPath : sourceFiles.includeDirectories()) {
            final HierarchicalResource includeDirectory = context.require(includeDirectoryPath, ResourceStampers.<HierarchicalResource>exists());
            if(!includeDirectory.exists() || !includeDirectory.isDirectory()) {
                return Result.ofErr(SpoofaxStrategoConfigureException.includeDirectoryFail(includeDirectoryPath));
            }
        }

        // Gather origins for provided Stratego files.
        final ArrayList<STask<?>> sourceFileOrigins = new ArrayList<>();

        // Gather include directories, str2libs, and Java classpath.
        final LinkedHashSet<ResourcePath> includeDirectories = new LinkedHashSet<>(); // LinkedHashSet to remove duplicates while keeping insertion order.
        includeDirectories.add(sourceFiles.mainSourceDirectory()); // Add main source directory as an include for imports.
        includeDirectories.addAll(sourceFiles.includeDirectories());
        final LinkedHashSet<Supplier<Stratego2LibInfo>> str2Libs = new LinkedHashSet<>();
        final Supplier<StrategoLibInfo> strategoLibInfoSupplier = strategoLibUtil.getStrategoLibInfo(sourceFiles.strategoLibUnarchiveDirectory(), unarchiveFromJar);
        str2Libs.add(strategoLibInfoSupplier.map(new FromStrategoLibInfoToStratego2LibInfo()));
        final Supplier<GppInfo> gppInfoSupplier = gppUtil.getGppInfo(sourceFiles.gppUnarchiveDirectory(), unarchiveFromJar);
        str2Libs.add(gppInfoSupplier.map(new FromGppToStratego2LibInfo()));
        final LinkedHashSet<File> javaClassPaths = new LinkedHashSet<>();
        javaClassPaths.addAll(strategoLibUtil.getStrategoLibJavaClassPaths());
        javaClassPaths.addAll(gppUtil.getGppJavaClassPaths());

        final Task<Result<ListView<ResourcePath>, ResolveIncludesException>> resolveIncludesTask =
            resolveIncludes.createTask(new SpoofaxStrategoResolveIncludes.Input(rootDirectory, sourceFiles.unarchiveDirectory()));
        sourceFileOrigins.add(resolveIncludesTask.toSupplier());
        final Result<ListView<ResourcePath>, ResolveIncludesException> result =
            context.require(resolveIncludesTask);
        if(result.isErr()) {
            // noinspection ConstantConditions (err is present)
            return Result.ofErr(SpoofaxStrategoConfigureException.resolveIncludeFail(result.getErr()));
        } else {
            // noinspection ConstantConditions (value is present)
            result.get().addAllTo(includeDirectories);
        }

        // Compile each SDF3 source file (if SDF3 is enabled) to a Stratego signature, pretty-printer, completion
        // runtime, and injection explication (if enabled) module.
        final ResourcePath generatedSourcesDirectory = sourceFiles.generatedSourcesDirectory();
        final String strategyAffix = cfgStrategoConfig.languageStrategyAffix();
        try {
            spoofaxSdf3GenerationUtil.performSdf3GenerationIfEnabled(context, rootDirectory, new SpoofaxSdf3GenerationUtil.Callbacks<SpoofaxStrategoConfigureException>() {
                @Override
                public void generateFromAst(ExecContext context, STask<Result<IStrategoTerm, ?>> astSupplier) throws SpoofaxStrategoConfigureException, InterruptedException {
                    try {
                        sdf3ToPrettyPrinter(context, strategyAffix, generatedSourcesDirectory, astSupplier);
                    } catch(RuntimeException | InterruptedException e) {
                        throw e; // Do not wrap runtime and interrupted exceptions, rethrow them.
                    } catch(Exception e) {
                        throw SpoofaxStrategoConfigureException.sdf3PrettyPrinterGenerateFail(e);
                    }
                    // HACK: for now disabled completion runtime generation, as it is not used in Spoofax 3 (yet?)
//                    try {
//                        sdf3ToCompletionRuntime(context, generatedSourcesDirectory, astSupplier);
//                    } catch(RuntimeException | InterruptedException e) {
//                        throw e; // Do not wrap runtime and interrupted exceptions, rethrow them.
//                    } catch(Exception e) {
//                        throw StrategoConfigureException.sdf3CompletionRuntimeGenerateFail(e);
//                    }
                    if(cfgStrategoConfig.enableSdf3StatixExplicationGen()) {
                        try {
                            sdf3ToStatixGenInj(context, strategyAffix, generatedSourcesDirectory, astSupplier);
                        } catch(RuntimeException | InterruptedException e) {
                            throw e; // Do not wrap runtime and interrupted exceptions, rethrow them.
                        } catch(Exception e) {
                            throw SpoofaxStrategoConfigureException.sdf3ExtStatixGenInjFail(e);
                        }
                    }
                }

                @Override
                public void generateFromAnalyzed(ExecContext context, Supplier<Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>> singleFileAnalysisOutputSupplier) throws SpoofaxStrategoConfigureException, InterruptedException {
                    try {
                        sdf3ToSignature(context, generatedSourcesDirectory, singleFileAnalysisOutputSupplier);
                    } catch(RuntimeException | InterruptedException e) {
                        throw e; // Do not wrap runtime and interrupted exceptions, rethrow them.
                    } catch(Exception e) {
                        throw SpoofaxStrategoConfigureException.sdf3SignatureGenerateFail(e);
                    }
                }

                @Override
                public void generateFromConfig(ExecContext context, Sdf3SpecConfig sdf3Config) throws SpoofaxStrategoConfigureException, IOException, InterruptedException {
                    // Compile SDF3 specification to a Stratego parenthesizer.
                    try {
                        final STask<Result<ParseTable, ?>> parseTableSupplier = sdf3ToParseTable.createSupplier(new Sdf3SpecToParseTable.Input(sdf3Config, false));
                        sdf3ToParenthesizer(context, strategyAffix, generatedSourcesDirectory, parseTableSupplier);
                    } catch(RuntimeException | InterruptedException e) {
                        throw e; // Do not wrap runtime and interrupted exceptions, rethrow them.
                    } catch(Exception e) {
                        throw SpoofaxStrategoConfigureException.sdf3ParenthesizerGenerateFail(e);
                    }

                    { // Generate pp and completion Stratego module.
                        final HashMap<String, Object> map = new HashMap<>();
                        map.put("name", strategyAffix);
                        map.put("ppName", strategyAffix);
                        map.put("sdf3MainModule", sdf3Config.getMainModuleName());
                        completionTemplate.write(context, generatedSourcesDirectory.appendRelativePath("completion.str2"), cfgStrategoConfig, map);
                        ppTemplate.write(context, generatedSourcesDirectory.appendRelativePath("pp.str2"), cfgStrategoConfig, map);
                    }

                    // Add generated sources directory as an include for Stratego imports.
                    includeDirectories.add(generatedSourcesDirectory);
                    // Add this as an origin, as this task provides the Stratego files (in strategoGenerationUtil.writePrettyPrintedFile).
                    sourceFileOrigins.add(createSupplier(rootDirectory));
                }
            });
        } catch(SpoofaxStrategoConfigureException e) {
            return Result.ofErr(e);
        } catch(SpoofaxSdf3ConfigureException e) {
            return Result.ofErr(SpoofaxStrategoConfigureException.sdf3ConfigureFail(e));
        }

        final ArrayList<BuiltinLibraryIdentifier> builtinLibraryIdentifiers = new ArrayList<>(sourceFiles.includeBuiltinLibraries().size());
        for(String builtinLibraryName : sourceFiles.includeBuiltinLibraries()) {
            final @Nullable BuiltinLibraryIdentifier identifier = BuiltinLibraryIdentifier.fromString(builtinLibraryName);
            if(identifier == null) {
                return Result.ofErr(SpoofaxStrategoConfigureException.builtinLibraryFail(builtinLibraryName));
            }
            builtinLibraryIdentifiers.add(identifier);
        }

        return Result.ofOk(new StrategoCompileConfig(
            rootDirectory,
            new ModuleIdentifier(true, false, sourceFiles.mainModule(), mainFile.getPath()),
            ListView.copyOf(includeDirectories),
            ListView.of(builtinLibraryIdentifiers),
            ListView.copyOf(str2Libs),
            new Arguments(), // TODO: add to input and configure
            ListView.of(sourceFileOrigins),
            null, //strategoInput.cacheDirectory(), // TODO: settings this crashes the compiler, most likely due to the ## symbols in the path.
            cfgStrategoConfig.javaSourceFileOutputDirectory(),
            cfgStrategoConfig.javaClassFileOutputDirectory(),
            cfgStrategoConfig.outputJavaPackageId(),
            cfgStrategoConfig.outputLibraryName(),
            ListView.copyOf(javaClassPaths)
        ));
    }

    private void sdf3ToSignature(
        ExecContext context,
        ResourcePath generatesSourcesDirectory,
        Supplier<Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>> singleFileAnalysisOutputSupplier
    ) throws Exception {
        final STask<Result<IStrategoTerm, ?>> supplier = sdf3ToSignature.createSupplier(singleFileAnalysisOutputSupplier);
        spoofaxStrategoGenerationUtil.writePrettyPrintedFile(context, generatesSourcesDirectory, supplier);
    }

    private void sdf3ToPrettyPrinter(
        ExecContext context,
        String strategyAffix,
        ResourcePath generatesSourcesDirectory,
        STask<Result<IStrategoTerm, ?>> astSupplier
    ) throws Exception {
        final STask<Result<IStrategoTerm, ?>> supplier = sdf3ToPrettyPrinter.createSupplier(new Sdf3ToPrettyPrinter.Input(astSupplier, strategyAffix));
        spoofaxStrategoGenerationUtil.writePrettyPrintedFile(context, generatesSourcesDirectory, supplier);
    }

    private void sdf3ToParenthesizer(
        ExecContext context,
        String strategyAffix,
        ResourcePath generatesSourcesDirectory,
        STask<Result<ParseTable, ?>> parseTableSupplier
    ) throws Exception {
        final STask<Result<IStrategoTerm, ?>> supplier = sdf3ToParenthesizer.createSupplier(new Sdf3ParseTableToParenthesizer.Args(parseTableSupplier, strategyAffix));
        spoofaxStrategoGenerationUtil.writePrettyPrintedFile(context, generatesSourcesDirectory, supplier);
    }

    private void sdf3ToCompletionRuntime(
        ExecContext context,
        ResourcePath generatesSourcesDirectory,
        STask<Result<IStrategoTerm, ?>> astSupplier
    ) throws Exception {
        final STask<Result<IStrategoTerm, ?>> supplier = sdf3ToCompletionRuntime.createSupplier(astSupplier);
        spoofaxStrategoGenerationUtil.writePrettyPrintedFile(context, generatesSourcesDirectory, supplier);
    }

    private void sdf3ToStatixGenInj(
        ExecContext context,
        String strategyAffix,
        ResourcePath generatesSourcesDirectory,
        STask<Result<IStrategoTerm, ?>> astSupplier
    ) throws Exception {
        final STask<Result<IStrategoTerm, ?>> supplier = sdf3ExtStatixGenerateStratego.createSupplier(new Sdf3ExtStatixGenerateStratego.Input(astSupplier, strategyAffix));
        spoofaxStrategoGenerationUtil.writePrettyPrintedFile(context, generatesSourcesDirectory, supplier);
    }

    private static class FromStrategoLibInfoToStratego2LibInfo extends StatelessSerializableFunction<StrategoLibInfo, Stratego2LibInfo> {
        @Override public Stratego2LibInfo apply(StrategoLibInfo strategoLibInfo) {
            return new Stratego2LibInfo(strategoLibInfo.str2libFile, strategoLibInfo.jarFilesOrDirectories);
        }
    }

    private static class FromGppToStratego2LibInfo extends StatelessSerializableFunction<GppInfo, Stratego2LibInfo> {
        @Override public Stratego2LibInfo apply(GppInfo gppInfo) {
            return new Stratego2LibInfo(gppInfo.str2libFile, gppInfo.jarFilesOrDirectories);
        }
    }
}
