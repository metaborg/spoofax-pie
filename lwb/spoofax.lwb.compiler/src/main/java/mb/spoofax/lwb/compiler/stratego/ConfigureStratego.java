package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.CompileLanguageInput;
import mb.cfg.CompileLanguageSpecificationInput;
import mb.cfg.CompileLanguageSpecificationShared;
import mb.cfg.metalang.CompileStrategoInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.common.util.StreamIterable;
import mb.constraint.pie.ConstraintAnalyzeMultiTaskDef;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.libspoofax2.LibSpoofax2ClassLoaderResources;
import mb.libspoofax2.LibSpoofax2Exports;
import mb.libstatix.LibStatixClassLoaderResources;
import mb.libstatix.LibStatixExports;
import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3ToCompletionRuntime;
import mb.sdf3.task.Sdf3ToPrettyPrinter;
import mb.sdf3.task.Sdf3ToSignature;
import mb.sdf3.task.spec.Sdf3ParseTableToParenthesizer;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spec.Sdf3SpecToParseTable;
import mb.sdf3.task.util.Sdf3Util;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.spoofax.lwb.compiler.sdf3.ConfigureSdf3;
import mb.spoofax.lwb.compiler.sdf3.Sdf3ConfigureException;
import mb.str.config.StrategoCompileConfig;
import mb.str.task.StrategoPrettyPrint;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.ModuleIdentifier;
import mb.stratego.build.util.StrategoGradualSetting;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

public class ConfigureStratego implements TaskDef<ResourcePath, Result<Option<StrategoCompileConfig>, StrategoConfigureException>> {
    private final TemplateWriter completionTemplate;
    private final TemplateWriter ppTemplate;

    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final UnarchiveFromJar unarchiveFromJar;
    private final LibSpoofax2ClassLoaderResources libSpoofax2ClassLoaderResources;
    private final LibStatixClassLoaderResources libStatixClassLoaderResources;

    private final StrategoPrettyPrint strategoPrettyPrint;

    private final ConfigureSdf3 configureSdf3;
    private final Sdf3Parse sdf3Parse;
    private final Sdf3Desugar sdf3Desugar;
    private final Sdf3AnalyzeMulti sdf3Analyze;
    private final Sdf3SpecToParseTable sdf3ToParseTable;
    private final Sdf3ToSignature sdf3ToSignature;
    private final Sdf3ToPrettyPrinter sdf3ToPrettyPrinter;
    private final Sdf3ParseTableToParenthesizer sdf3ToParenthesizer;
    private final Sdf3ToCompletionRuntime sdf3ToCompletionRuntime;


    @Inject public ConfigureStratego(
        TemplateCompiler templateCompiler,

        CfgRootDirectoryToObject cfgRootDirectoryToObject,

        UnarchiveFromJar unarchiveFromJar,
        LibSpoofax2ClassLoaderResources libSpoofax2ClassLoaderResources,
        LibStatixClassLoaderResources libStatixClassLoaderResources,

        StrategoPrettyPrint strategoPrettyPrint,

        ConfigureSdf3 configureSdf3,
        Sdf3Parse sdf3Parse,
        Sdf3Desugar sdf3Desugar,
        Sdf3AnalyzeMulti sdf3Analyze,
        Sdf3SpecToParseTable sdf3ToParseTable,
        Sdf3ToSignature sdf3ToSignature,
        Sdf3ToPrettyPrinter sdf3ToPrettyPrinter,
        Sdf3ParseTableToParenthesizer sdf3ToParenthesizer,
        Sdf3ToCompletionRuntime sdf3ToCompletionRuntime
    ) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.completionTemplate = templateCompiler.getOrCompileToWriter("completion.str.mustache");
        this.ppTemplate = templateCompiler.getOrCompileToWriter("pp.str.mustache");

        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;

        this.unarchiveFromJar = unarchiveFromJar;
        this.libSpoofax2ClassLoaderResources = libSpoofax2ClassLoaderResources;
        this.libStatixClassLoaderResources = libStatixClassLoaderResources;

        this.strategoPrettyPrint = strategoPrettyPrint;

        this.configureSdf3 = configureSdf3;
        this.sdf3Parse = sdf3Parse;
        this.sdf3Desugar = sdf3Desugar;
        this.sdf3Analyze = sdf3Analyze;
        this.sdf3ToParseTable = sdf3ToParseTable;
        this.sdf3ToSignature = sdf3ToSignature;
        this.sdf3ToPrettyPrinter = sdf3ToPrettyPrinter;
        this.sdf3ToParenthesizer = sdf3ToParenthesizer;
        this.sdf3ToCompletionRuntime = sdf3ToCompletionRuntime;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Option<StrategoCompileConfig>, StrategoConfigureException> exec(ExecContext context, ResourcePath rootDirectory) throws Exception {
        return context.require(cfgRootDirectoryToObject, rootDirectory)
            .mapErr(StrategoConfigureException::getLanguageCompilerConfigurationFail)
            .<Option<StrategoCompileConfig>, Exception>flatMapThrowing(cfgOutput -> Result.transpose(Option.ofOptional(cfgOutput.compileLanguageInput.compileLanguageSpecificationInput().stratego())
                .mapThrowing(strategoInput -> toStrategoConfig(context, rootDirectory, cfgOutput.compileLanguageInput, strategoInput))
            ));
    }


    public Result<StrategoCompileConfig, StrategoConfigureException> toStrategoConfig(
        ExecContext context,
        ResourcePath rootDirectory,
        CompileLanguageInput input,
        CompileStrategoInput strategoInput
    ) throws IOException, InterruptedException {
        // TODO: move required properties into strategoInput.
        final CompileLanguageSpecificationInput compileLanguageSpecificationInput = input.compileLanguageSpecificationInput();
        final CompileLanguageSpecificationShared compileLanguageSpecificationShared = compileLanguageSpecificationInput.compileLanguageShared();

        // Check main source directory, main file, and include directories.
        final HierarchicalResource mainSourceDirectory = context.require(strategoInput.mainSourceDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainSourceDirectory.exists() || !mainSourceDirectory.isDirectory()) {
            return Result.ofErr(StrategoConfigureException.mainSourceDirectoryFail(mainSourceDirectory.getPath()));
        }
        final HierarchicalResource mainFile = context.require(strategoInput.mainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(StrategoConfigureException.mainFileFail(mainFile.getPath()));
        }
        for(ResourcePath includeDirectoryPath : strategoInput.includeDirectories()) {
            final HierarchicalResource includeDirectory = context.require(includeDirectoryPath, ResourceStampers.<HierarchicalResource>exists());
            if(!includeDirectory.exists() || !includeDirectory.isDirectory()) {
                return Result.ofErr(StrategoConfigureException.includeDirectoryFail(includeDirectoryPath));
            }
        }

        // Gather origins for provided Stratego files.
        final ArrayList<STask<?>> sourceFileOrigins = new ArrayList<>();

        // Gather include directories.
        final LinkedHashSet<ResourcePath> includeDirectories = new LinkedHashSet<>(); // LinkedHashSet to remove duplicates while keeping insertion order.
        includeDirectories.add(strategoInput.mainSourceDirectory()); // Add main source directory as an include for imports.
        includeDirectories.addAll(strategoInput.includeDirectories());

        // Determine libspoofax2 definition directories.
        final HashSet<HierarchicalResource> libSpoofax2DefinitionDirs = new LinkedHashSet<>(); // LinkedHashSet to remove duplicates while keeping insertion order.
        if(compileLanguageSpecificationShared.includeLibSpoofax2Exports()) {
            final ClassLoaderResourceLocations locations = libSpoofax2ClassLoaderResources.definitionDirectory.getLocations();
            libSpoofax2DefinitionDirs.addAll(locations.directories);
            final ResourcePath unarchiveDirectoryBase = compileLanguageSpecificationShared.unarchiveDirectory().appendRelativePath("libspoofax2");
            for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
                final ResourcePath jarFilePath = jarFileWithPath.file.getPath();
                @SuppressWarnings("ConstantConditions") // JAR files always have leaves.
                final ResourcePath unarchiveDirectory = unarchiveDirectoryBase.appendRelativePath(jarFilePath.getLeaf());
                final Task<?> task = unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, PathStringMatcher.ofExtension("str"), false, false));
                sourceFileOrigins.add(task.toSupplier());
                context.require(task); // HACK: eagerly unarchive such that the directory and contents exist.
                libSpoofax2DefinitionDirs.add(context.getHierarchicalResource(unarchiveDirectory.appendAsRelativePath(jarFileWithPath.path)));
            }
        }
        for(String export : LibSpoofax2Exports.getStrategoExports()) {
            for(HierarchicalResource definitionDir : libSpoofax2DefinitionDirs) {
                final HierarchicalResource exportDirectory = definitionDir.appendAsRelativePath(export);
                if(exportDirectory.exists()) {
                    includeDirectories.add(exportDirectory.getPath());
                }
            }
        }

        // Determine libstatix definition directories.
        final HashSet<HierarchicalResource> libStatixDefinitionDirs = new LinkedHashSet<>(); // LinkedHashSet to remove duplicates while keeping insertion order.
        if(compileLanguageSpecificationShared.includeLibStatixExports()) {
            final ClassLoaderResourceLocations locations = libStatixClassLoaderResources.definitionDirectory.getLocations();
            libStatixDefinitionDirs.addAll(locations.directories);
            final ResourcePath unarchiveDirectoryBase = compileLanguageSpecificationShared.unarchiveDirectory().appendRelativePath("libstatix");
            for(JarFileWithPath jarFileWithPath : locations.jarFiles) {
                final ResourcePath jarFilePath = jarFileWithPath.file.getPath();
                @SuppressWarnings("ConstantConditions") // JAR files always have leaves.
                final ResourcePath unarchiveDirectory = unarchiveDirectoryBase.appendRelativePath(jarFilePath.getLeaf());
                final Task<?> task = unarchiveFromJar.createTask(new UnarchiveFromJar.Input(jarFilePath, unarchiveDirectory, PathStringMatcher.ofExtension("str"), false, false));
                sourceFileOrigins.add(task.toSupplier());
                context.require(task); // HACK: eagerly unarchive such that the directory and contents exist.
                libStatixDefinitionDirs.add(context.getHierarchicalResource(unarchiveDirectory.appendAsRelativePath(jarFileWithPath.path)));
            }
        }
        for(String export : LibStatixExports.getStrategoExports()) {
            for(HierarchicalResource definitionDir : libStatixDefinitionDirs) {
                final HierarchicalResource exportDirectory = definitionDir.appendAsRelativePath(export);
                if(exportDirectory.exists()) {
                    includeDirectories.add(exportDirectory.getPath());
                }
            }
        }

        // Compile each SDF3 source file to a Stratego signature, pretty-printer, and completion runtime module (if SDF3 is enabled).
        final Result<Option<Sdf3SpecConfig>, Sdf3ConfigureException> configureSdf3Result = context.require(configureSdf3, rootDirectory);
        if(configureSdf3Result.isErr()) {
            // noinspection ConstantConditions (error is present)
            return Result.ofErr(StrategoConfigureException.sdf3ConfigureFail(configureSdf3Result.getErr()));
        }
        // noinspection ConstantConditions (value is present)
        final Option<Sdf3SpecConfig> configureSdf3Option = configureSdf3Result.get();
        // noinspection ConstantConditions (value is present)
        if(configureSdf3Option.isSome()) {
            // noinspection ConstantConditions (value is present)
            final Sdf3SpecConfig sdf3Config = configureSdf3Option.get();

            final ResourcePath generatedSourcesDirectory = compileLanguageSpecificationShared.generatedStrategoSourcesDirectory();
            final String strategyAffix = strategoInput.languageStrategyAffix();

            final ResourceWalker walker = Sdf3Util.createResourceWalker();
            final ResourceMatcher matcher = Sdf3Util.createResourceMatcher();
            final JsglrParseTaskInput.Builder parseInputBuilder = sdf3Parse.inputBuilder().rootDirectoryHint(rootDirectory);
            final Sdf3AnalyzeMulti.Input analyzeInput = new Sdf3AnalyzeMulti.Input(sdf3Config.mainSourceDirectory, sdf3Parse.createRecoverableMultiAstSupplierFunction(walker, matcher));
            final HierarchicalResource sdfMainSourceDirectory = context.require(sdf3Config.mainSourceDirectory, ResourceStampers.modifiedDirRec(walker, matcher));
            try(final Stream<? extends HierarchicalResource> stream = sdfMainSourceDirectory.walk(walker, matcher)) {
                for(HierarchicalResource file : new StreamIterable<>(stream)) {
                    final Supplier<Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>> singleFileAnalysisOutputSupplier = sdf3Analyze.createSingleFileOutputSupplier(analyzeInput, file.getPath());
                    try {
                        sdf3ToSignature(context, generatedSourcesDirectory, singleFileAnalysisOutputSupplier);
                    } catch(RuntimeException | InterruptedException e) {
                        throw e; // Do not wrap runtime and interrupted exceptions, rethrow them.
                    } catch(Exception e) {
                        return Result.ofErr(StrategoConfigureException.sdf3SignatureGenerateFail(e));
                    }

                    final STask<Result<IStrategoTerm, ?>> astSupplier = sdf3Desugar.createSupplier(parseInputBuilder.withFile(file.getPath()).buildAstSupplier());
                    try {
                        sdf3ToPrettyPrinter(context, strategyAffix, generatedSourcesDirectory, astSupplier);
                    } catch(RuntimeException | InterruptedException e) {
                        throw e; // Do not wrap runtime and interrupted exceptions, rethrow them.
                    } catch(Exception e) {
                        return Result.ofErr(StrategoConfigureException.sdf3PrettyPrinterGenerateFail(e));
                    }
                    try {
                        sdf3ToCompletionRuntime(context, generatedSourcesDirectory, astSupplier);
                    } catch(RuntimeException | InterruptedException e) {
                        throw e; // Do not wrap runtime and interrupted exceptions, rethrow them.
                    } catch(Exception e) {
                        return Result.ofErr(StrategoConfigureException.sdf3CompletionRuntimeGenerateFail(e));
                    }
                }
            }

            // Compile SDF3 specification to a Stratego parenthesizer.
            try {
                final STask<Result<ParseTable, ?>> parseTableSupplier = sdf3ToParseTable.createSupplier(new Sdf3SpecToParseTable.Input(sdf3Config, false));
                sdf3ToParenthesizer(context, strategyAffix, generatedSourcesDirectory, parseTableSupplier);
            } catch(RuntimeException | InterruptedException e) {
                throw e; // Do not wrap runtime and interrupted exceptions, rethrow them.
            } catch(Exception e) {
                return Result.ofErr(StrategoConfigureException.sdf3ParenthesizerGenerateFail(e));
            }

            { // Generate pp and completion Stratego module.
                final HashMap<String, Object> map = new HashMap<>();
                map.put("name", strategyAffix);
                map.put("ppName", strategyAffix);
                completionTemplate.write(context, generatedSourcesDirectory.appendRelativePath("completion.str"), input, map);
                ppTemplate.write(context, generatedSourcesDirectory.appendRelativePath("pp.str"), input, map);
            }

            // Add generated sources directory as an include for Stratego imports.
            includeDirectories.add(generatedSourcesDirectory);
            // Add this as an origin, as this task provides the Stratego files (in writePrettyPrintedStrategoFile).
            sourceFileOrigins.add(this.createSupplier(rootDirectory));
        }

        final ArrayList<BuiltinLibraryIdentifier> builtinLibraryIdentifiers = new ArrayList<>(strategoInput.includeBuiltinLibraries().size());
        for(String builtinLibraryName : strategoInput.includeBuiltinLibraries()) {
            final @Nullable BuiltinLibraryIdentifier identifier = BuiltinLibraryIdentifier.fromString(builtinLibraryName);
            if(identifier == null) {
                return Result.ofErr(StrategoConfigureException.builtinLibraryFail(builtinLibraryName));
            }
            builtinLibraryIdentifiers.add(identifier);
        }

        return Result.ofOk(new StrategoCompileConfig(
            rootDirectory,
            new ModuleIdentifier(false, strategoInput.mainModule(), mainFile.getPath()),
            ListView.copyOf(includeDirectories),
            ListView.of(builtinLibraryIdentifiers),
            StrategoGradualSetting.NONE, // TODO: add to input and configure
            new Arguments(), // TODO: add to input and configure
            ListView.of(sourceFileOrigins),
            null, //strategoInput.cacheDirectory(), // TODO: settings this crashes the compiler, most likely due to the ## symbols in the path.
            strategoInput.outputDirectory(),
            strategoInput.outputJavaPackageId()
        ));
    }

    private void sdf3ToSignature(
        ExecContext context,
        ResourcePath generatesSourcesDirectory,
        Supplier<Result<ConstraintAnalyzeMultiTaskDef.SingleFileOutput, ?>> singleFileAnalysisOutputSupplier
    ) throws Exception {
        final STask<Result<IStrategoTerm, ?>> supplier = sdf3ToSignature.createSupplier(singleFileAnalysisOutputSupplier);
        writePrettyPrintedStrategoFile(context, generatesSourcesDirectory, supplier);
    }

    private void sdf3ToPrettyPrinter(
        ExecContext context,
        String strategyAffix,
        ResourcePath generatesSourcesDirectory,
        STask<Result<IStrategoTerm, ?>> astSupplier
    ) throws Exception {
        final STask<Result<IStrategoTerm, ?>> supplier = sdf3ToPrettyPrinter.createSupplier(new Sdf3ToPrettyPrinter.Input(astSupplier, strategyAffix));
        writePrettyPrintedStrategoFile(context, generatesSourcesDirectory, supplier);
    }

    private void sdf3ToParenthesizer(
        ExecContext context,
        String strategyAffix,
        ResourcePath generatesSourcesDirectory,
        STask<Result<ParseTable, ?>> parseTableSupplier
    ) throws Exception {
        final STask<Result<IStrategoTerm, ?>> supplier = sdf3ToParenthesizer.createSupplier(new Sdf3ParseTableToParenthesizer.Args(parseTableSupplier, strategyAffix));
        writePrettyPrintedStrategoFile(context, generatesSourcesDirectory, supplier);
    }

    private void sdf3ToCompletionRuntime(
        ExecContext context,
        ResourcePath generatesSourcesDirectory,
        STask<Result<IStrategoTerm, ?>> astSupplier
    ) throws Exception {
        final STask<Result<IStrategoTerm, ?>> supplier = sdf3ToCompletionRuntime.createSupplier(astSupplier);
        writePrettyPrintedStrategoFile(context, generatesSourcesDirectory, supplier);
    }

    private void writePrettyPrintedStrategoFile(
        ExecContext context,
        ResourcePath generatesSourcesDirectory,
        STask<Result<IStrategoTerm, ?>> supplier
    ) throws Exception {
        final Result<IStrategoTerm, ? extends Exception> result = context.require(supplier);
        final IStrategoTerm ast = result.unwrap();
        final String moduleName = TermUtils.toJavaStringAt(ast, 0);

        final Result<IStrategoTerm, ? extends Exception> prettyPrintedResult = context.require(strategoPrettyPrint, supplier);
        final IStrategoTerm prettyPrintedTerm = prettyPrintedResult.unwrap();
        final String prettyPrinted = TermUtils.toJavaString(prettyPrintedTerm);

        final HierarchicalResource file = context.getHierarchicalResource(generatesSourcesDirectory.appendRelativePath(moduleName).appendToLeaf(".str"));
        file.ensureFileExists();
        file.writeString(prettyPrinted);
        context.provide(file);
    }
}
