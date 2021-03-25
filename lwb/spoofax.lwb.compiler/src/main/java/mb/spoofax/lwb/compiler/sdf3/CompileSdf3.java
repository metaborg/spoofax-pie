package mb.spoofax.lwb.compiler.sdf3;

import mb.cfg.metalang.CompileSdf3Input;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.StreamIterable;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.ValueSupplier;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.AllResourceMatcher;
import mb.resource.hierarchical.match.FileResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3ToCompletionRuntime;
import mb.sdf3.task.Sdf3ToPrettyPrinter;
import mb.sdf3.task.Sdf3ToSignature;
import mb.sdf3.task.spec.Sdf3CreateSpec;
import mb.sdf3.task.spec.Sdf3ParseTableToFile;
import mb.sdf3.task.spec.Sdf3ParseTableToParenthesizer;
import mb.sdf3.task.spec.Sdf3Spec;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spec.Sdf3SpecToParseTable;
import mb.sdf3.task.spoofax.Sdf3CheckMulti;
import mb.sdf3.task.util.Sdf3Util;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.compiler.util.TemplateWriter;
import mb.str.task.StrategoPrettyPrint;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.stream.Stream;

import static mb.constraint.pie.ConstraintAnalyzeMultiTaskDef.SingleFileOutput;

public class CompileSdf3 implements TaskDef<CompileSdf3Input, Result<KeyedMessages, Sdf3CompileException>> {
    private final TemplateWriter completionTemplate;
    private final TemplateWriter ppTemplate;
    private final Sdf3Parse parse;
    private final Function<Supplier<? extends Result<IStrategoTerm, ?>>, Result<IStrategoTerm, ?>> desugar;
    private final Sdf3AnalyzeMulti analyze;
    private final Sdf3CheckMulti check;
    private final Sdf3CreateSpec createSpec;
    private final Sdf3SpecToParseTable toParseTable;
    private final Sdf3ParseTableToFile parseTableToFile;
    private final StrategoPrettyPrint strategoPrettyPrint;
    private final Sdf3ToSignature toSignature;
    private final Sdf3ToPrettyPrinter toPrettyPrinter;
    private final Sdf3ParseTableToParenthesizer toParenthesizer;
    private final Sdf3ToCompletionRuntime toCompletionRuntime;

    @Inject public CompileSdf3(
        TemplateCompiler templateCompiler,
        Sdf3Parse parse,
        Sdf3Desugar desugar,
        Sdf3AnalyzeMulti analyze,
        Sdf3CreateSpec createSpec,
        Sdf3CheckMulti check,
        Sdf3SpecToParseTable toParseTable,
        Sdf3ParseTableToFile parseTableToFile,
        StrategoPrettyPrint strategoPrettyPrint,
        Sdf3ToSignature toSignature,
        Sdf3ToPrettyPrinter toPrettyPrinter,
        Sdf3ParseTableToParenthesizer toParenthesizer,
        Sdf3ToCompletionRuntime toCompletionRuntime
    ) {
        templateCompiler = templateCompiler.loadingFromClass(getClass());
        this.completionTemplate = templateCompiler.getOrCompileToWriter("completion.str.mustache");
        this.ppTemplate = templateCompiler.getOrCompileToWriter("pp.str.mustache");
        this.parse = parse;
        this.desugar = desugar.createFunction();
        this.analyze = analyze;
        this.check = check;
        this.createSpec = createSpec;
        this.toParseTable = toParseTable;
        this.parseTableToFile = parseTableToFile;
        this.strategoPrettyPrint = strategoPrettyPrint;
        this.toSignature = toSignature;
        this.toPrettyPrinter = toPrettyPrinter;
        this.toParenthesizer = toParenthesizer;
        this.toCompletionRuntime = toCompletionRuntime;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, Sdf3CompileException> exec(ExecContext context, CompileSdf3Input input) throws Exception {
        // Check main file and root directory.
        final HierarchicalResource mainFile = context.require(input.mainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(Sdf3CompileException.mainFileFail(mainFile.getPath()));
        }
        final HierarchicalResource sourceDirectory = context.require(input.sourceDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            return Result.ofErr(Sdf3CompileException.sourceDirectoryFail(sourceDirectory.getPath()));
        }
        final ResourcePath rootDirectory = input.rootDirectory();

        // Check SDF3 source files.
        final ResourceWalker resourceWalker = Sdf3Util.createResourceWalker();
        final ResourceMatcher resourceMatcher = new AllResourceMatcher(Sdf3Util.createResourceMatcher(), new FileResourceMatcher());
        final @Nullable KeyedMessages messages = context.require(check.createTask(
            // HACK: Run check on root directory so it can pick up the CFG file.
            new Sdf3CheckMulti.Input(rootDirectory, resourceWalker, resourceMatcher)
        ));
        if(messages.containsError()) {
            return Result.ofErr(Sdf3CompileException.checkFail(messages));
        }

        // Compile SDF3 spec to a parse table.
        final ParseTableConfiguration parseTableConfiguration = new ParseTableConfiguration(
            input.createDynamicParseTable(),
            input.createDataDependentParseTable(),
            input.solveDeepConflictsInParseTable(),
            input.checkOverlapInParseTable(),
            input.checkPrioritiesInParseTable(),
            input.createLayoutSensitiveParseTable()
        );
        final Supplier<Result<Sdf3SpecConfig, ?>> specConfigSupplier = new ValueSupplier<>(Result.ofOk(new Sdf3SpecConfig(input.sourceDirectory(), input.mainFile(), parseTableConfiguration)));
        final Supplier<Result<Sdf3Spec, ?>> specSupplier = createSpec.createSupplier(specConfigSupplier);
        final Supplier<Result<ParseTable, ?>> parseTableSupplier = toParseTable.createSupplier(new Sdf3SpecToParseTable.Input(specSupplier, false));
        final Result<None, ? extends Exception> compileResult = context.require(parseTableToFile, new Sdf3ParseTableToFile.Args(parseTableSupplier, input.parseTableOutputFile()));
        if(compileResult.isErr()) {
            return Result.ofErr(Sdf3CompileException.parseTableCompileFail(compileResult.getErr()));
        }

        // Compile each SDF3 source file to a Stratego signature, pretty-printer, and completion runtime module.
        final Sdf3AnalyzeMulti.Input analyzeInput = new Sdf3AnalyzeMulti.Input(sourceDirectory.getPath(), parse.createRecoverableMultiAstSupplierFunction(resourceWalker, resourceMatcher));
        try(final Stream<? extends HierarchicalResource> stream = sourceDirectory.walk(resourceWalker, resourceMatcher)) {
            for(HierarchicalResource file : new StreamIterable<>(stream)) {
                final Supplier<Result<SingleFileOutput, ?>> singleFileAnalysisOutputSupplier = analyze.createSingleFileOutputSupplier(analyzeInput, file.getPath());
                try {
                    toSignature(context, input, singleFileAnalysisOutputSupplier);
                } catch(Exception e) {
                    return Result.ofErr(Sdf3CompileException.signatureGenerateFail(e));
                }

                final Supplier<Result<IStrategoTerm, ?>> astSupplier = desugar.createSupplier(parse.inputBuilder().withFile(file.getPath()).rootDirectoryHint(rootDirectory).buildAstSupplier());
                try {
                    toPrettyPrinter(context, input, astSupplier);
                } catch(Exception e) {
                    return Result.ofErr(Sdf3CompileException.prettyPrinterGenerateFail(e));
                }
                try {
                    toCompletionRuntime(context, input, astSupplier);
                } catch(Exception e) {
                    return Result.ofErr(Sdf3CompileException.completionRuntimeGenerateFail(e));
                }
            }
        }

        // Compile SDF3 spec to a parenthesizer.
        try {
            toParenthesizer(context, input, parseTableSupplier);
        } catch(RuntimeException e) {
            throw e; // Do not wrap runtime exceptions, rethrow them.
        } catch(Exception e) {
            return Result.ofErr(Sdf3CompileException.parenthesizerGenerateFail(e));
        }

        { // Generate pp and completion Stratego module.
            final HashMap<String, Object> map = new HashMap<>();
            map.put("name", input.strategoStrategyIdAffix());
            map.put("ppName", input.strategoStrategyIdAffix());
            final ResourcePath generatedStrategoSourcesDirectory = input.compileLanguageShared().generatedStrategoSourcesDirectory();
            completionTemplate.write(context, generatedStrategoSourcesDirectory.appendRelativePath("completion.str"), input, map);
            ppTemplate.write(context, generatedStrategoSourcesDirectory.appendRelativePath("pp.str"), input, map);
        }

        return Result.ofOk(messages);
    }

    private void toSignature(ExecContext context, CompileSdf3Input input, Supplier<Result<SingleFileOutput, ?>> singleFileAnalysisOutputSupplier) throws Exception {
        final Supplier<Result<IStrategoTerm, ?>> supplier = toSignature.createSupplier(singleFileAnalysisOutputSupplier);
        writePrettyPrintedStrategoFile(context, input, supplier);
    }

    private void toPrettyPrinter(ExecContext context, CompileSdf3Input input, Supplier<Result<IStrategoTerm, ?>> astSupplier) throws Exception {
        final Supplier<Result<IStrategoTerm, ?>> supplier = toPrettyPrinter.createSupplier(new Sdf3ToPrettyPrinter.Input(astSupplier, input.strategoStrategyIdAffix()));
        writePrettyPrintedStrategoFile(context, input, supplier);
    }

    private void toParenthesizer(ExecContext context, CompileSdf3Input input, Supplier<Result<ParseTable, ?>> parseTableSupplier) throws Exception {
        final Supplier<Result<IStrategoTerm, ?>> supplier = toParenthesizer.createSupplier(new Sdf3ParseTableToParenthesizer.Args(parseTableSupplier, input.strategoStrategyIdAffix()));
        writePrettyPrintedStrategoFile(context, input, supplier);
    }

    private void toCompletionRuntime(ExecContext context, CompileSdf3Input input, Supplier<Result<IStrategoTerm, ?>> astSupplier) throws Exception {
        final Supplier<Result<IStrategoTerm, ?>> supplier = toCompletionRuntime.createSupplier(astSupplier);
        writePrettyPrintedStrategoFile(context, input, supplier);
    }

    private void writePrettyPrintedStrategoFile(ExecContext context, CompileSdf3Input input, Supplier<Result<IStrategoTerm, ?>> supplier) throws Exception {
        final Result<IStrategoTerm, ? extends Exception> result = context.require(supplier);
        final IStrategoTerm ast = result.unwrap();
        final String moduleName = TermUtils.toJavaStringAt(ast, 0);

        final Result<IStrategoTerm, ? extends Exception> prettyPrintedResult = context.require(strategoPrettyPrint, supplier);
        final IStrategoTerm prettyPrintedTerm = prettyPrintedResult.unwrap();
        final String prettyPrinted = TermUtils.toJavaString(prettyPrintedTerm);

        final HierarchicalResource file = context.getHierarchicalResource(input.compileLanguageShared().generatedStrategoSourcesDirectory().appendRelativePath(moduleName).appendToLeaf(".str"));
        file.ensureFileExists();
        file.writeString(prettyPrinted);
        context.provide(file);
    }
}
