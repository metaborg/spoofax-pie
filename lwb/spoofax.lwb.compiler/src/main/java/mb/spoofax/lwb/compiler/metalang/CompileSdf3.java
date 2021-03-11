package mb.spoofax.lwb.compiler.metalang;

import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.ADT;
import mb.common.util.StreamIterable;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.ValueSupplier;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.AllResourceMatcher;
import mb.resource.hierarchical.match.FileResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.Sdf3ToCompletionColorer;
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
import mb.spoofx.lwb.compiler.cfg.metalang.CompileSdf3Input;
import mb.str.task.StrategoPrettyPrint;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static mb.constraint.pie.ConstraintAnalyzeMultiTaskDef.SingleFileOutput;

@Value.Enclosing
public class CompileSdf3 implements TaskDef<CompileSdf3Input, Result<CompileSdf3.Output, CompileSdf3.Sdf3CompileException>> {
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
    private final Sdf3ToCompletionColorer toCompletionColorer;

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
        Sdf3ToCompletionRuntime toCompletionRuntime,
        Sdf3ToCompletionColorer toCompletionColorer
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
        this.toCompletionColorer = toCompletionColorer;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Output, Sdf3CompileException> exec(ExecContext context, CompileSdf3Input input) throws Exception {
        // Check main file and root directory.
        final HierarchicalResource mainFile = context.require(input.sdf3MainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(Sdf3CompileException.mainFileFail(mainFile.getPath()));
        }
        final HierarchicalResource rootDirectory = context.require(input.sdf3RootDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            return Result.ofErr(Sdf3CompileException.rootDirectoryFail(rootDirectory.getPath()));
        }

        // Check SDF3 source files.
        // TODO: this does not check ESV files in include directories.
        final ResourceWalker resourceWalker = Sdf3Util.createResourceWalker();
        final ResourceMatcher resourceMatcher = new AllResourceMatcher(Sdf3Util.createResourceMatcher(), new FileResourceMatcher());
        final @Nullable KeyedMessages messages = context.require(check.createTask(
            new Sdf3CheckMulti.Input(rootDirectory.getPath(), resourceWalker, resourceMatcher)
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
        final Supplier<Result<Sdf3SpecConfig, ?>> specConfigSupplier = new ValueSupplier<>(Result.ofOk(new Sdf3SpecConfig(input.sdf3RootDirectory(), input.sdf3MainFile(), parseTableConfiguration)));
        final Supplier<Result<Sdf3Spec, ?>> specSupplier = createSpec.createSupplier(specConfigSupplier);
        final Supplier<Result<ParseTable, ?>> parseTableSupplier = toParseTable.createSupplier(new Sdf3SpecToParseTable.Input(specSupplier, false));
        final Result<None, ? extends Exception> compileResult = context.require(parseTableToFile, new Sdf3ParseTableToFile.Args(parseTableSupplier, input.sdf3ParseTableOutputFile()));
        if(compileResult.isErr()) {
            return Result.ofErr(Sdf3CompileException.parseTableCompileFail(compileResult.getErr()));
        }

        // Compile each SDF3 source file to a Stratego signature, pretty-printer, and completion runtime module.
        final ArrayList<Supplier<Result<IStrategoTerm, ?>>> esvCompletionColorerAstSuppliers = new ArrayList<>();
        final Sdf3AnalyzeMulti.Input analyzeInput = new Sdf3AnalyzeMulti.Input(rootDirectory.getPath(), resourceWalker, resourceMatcher, parse.createRecoverableAstFunction());
        try(final Stream<? extends HierarchicalResource> stream = rootDirectory.walk(resourceWalker, resourceMatcher)) {
            for(HierarchicalResource file : new StreamIterable<>(stream)) {
                final Supplier<Result<SingleFileOutput, ?>> singleFileAnalysisOutputSupplier = analyze.createSingleFileOutputSupplier(analyzeInput, file.getPath());
                try {
                    toSignature(context, input, singleFileAnalysisOutputSupplier);
                } catch(Exception e) {
                    return Result.ofErr(Sdf3CompileException.signatureGenerateFail(e));
                }

                final Supplier<Result<IStrategoTerm, ?>> astSupplier = desugar.createSupplier(parse.createAstSupplier(file.getPath()));
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
                esvCompletionColorerAstSuppliers.add(toCompletionColorer.createSupplier(astSupplier));
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

        return Result.ofOk(Output.builder()
            .messages(messages)
            .esvCompletionColorerAstSuppliers(esvCompletionColorerAstSuppliers)
            .build()
        );
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


    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends CompileSdf3Data.Output.Builder {}

        static Builder builder() { return new Builder(); }


        KeyedMessages messages();

        List<Supplier<Result<IStrategoTerm, ?>>> esvCompletionColorerAstSuppliers();
    }

    @ADT
    public abstract static class Sdf3CompileException extends Exception implements HasOptionalMessages {
        public interface Cases<R> {
            R mainFileFail(ResourceKey mainFile);

            R rootDirectoryFail(ResourcePath rootDirectory);

            R checkFail(KeyedMessages messages);

            R parseTableCompileFail(Exception cause);

            R signatureGenerateFail(Exception cause);

            R prettyPrinterGenerateFail(Exception cause);

            R parenthesizerGenerateFail(Exception cause);

            R completionRuntimeGenerateFail(Exception cause);
        }

        public static Sdf3CompileException mainFileFail(ResourceKey mainFile) {
            return Sdf3CompileExceptions.mainFileFail(mainFile);
        }

        public static Sdf3CompileException rootDirectoryFail(ResourcePath rootDirectory) {
            return Sdf3CompileExceptions.rootDirectoryFail(rootDirectory);
        }

        public static Sdf3CompileException checkFail(KeyedMessages messages) {
            return Sdf3CompileExceptions.checkFail(messages);
        }

        public static Sdf3CompileException parseTableCompileFail(Exception cause) {
            return withCause(Sdf3CompileExceptions.parseTableCompileFail(cause), cause);
        }

        public static Sdf3CompileException signatureGenerateFail(Exception cause) {
            return withCause(Sdf3CompileExceptions.signatureGenerateFail(cause), cause);
        }

        public static Sdf3CompileException prettyPrinterGenerateFail(Exception cause) {
            return withCause(Sdf3CompileExceptions.prettyPrinterGenerateFail(cause), cause);
        }

        public static Sdf3CompileException parenthesizerGenerateFail(Exception cause) {
            return withCause(Sdf3CompileExceptions.parenthesizerGenerateFail(cause), cause);
        }

        public static Sdf3CompileException completionRuntimeGenerateFail(Exception cause) {
            return withCause(Sdf3CompileExceptions.completionRuntimeGenerateFail(cause), cause);
        }

        private static Sdf3CompileException withCause(Sdf3CompileException e, Exception cause) {
            e.initCause(cause);
            return e;
        }


        public abstract <R> R match(Cases<R> cases);

        public Sdf3CompileExceptions.CasesMatchers.TotalMatcher_MainFileFail cases() {
            return Sdf3CompileExceptions.cases();
        }

        public Sdf3CompileExceptions.CaseOfMatchers.TotalMatcher_MainFileFail caseOf() {
            return Sdf3CompileExceptions.caseOf(this);
        }


        @Override public String getMessage() {
            return caseOf()
                .mainFileFail((mainFile) -> "SDF3 main file '" + mainFile + "' does not exist or is not a file")
                .rootDirectoryFail((rootDirectory) -> "SDF3 root directory '" + rootDirectory + "' does not exist or is not a directory")
                .checkFail((messages) -> "Parsing or checking SDF3 source files failed; see error messages")
                .parseTableCompileFail((cause) -> "Compile parse table from SDF3 failed unexpectedly")
                .signatureGenerateFail((cause) -> "Generate stratego signature from SDF3 failed unexpectedly")
                .prettyPrinterGenerateFail((cause) -> "Generate pretty-printer from SDF3 failed unexpectedly")
                .parenthesizerGenerateFail((cause) -> "Generate parenthesizer from SDF3 failed unexpectedly")
                .completionRuntimeGenerateFail((cause) -> "Generate completion runtime from SDF3 failed unexpectedly")
                ;
        }

        @Override public Throwable fillInStackTrace() {
            return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
        }

        @Override public Optional<KeyedMessages> getOptionalMessages() {
            return Sdf3CompileExceptions.getMessages(this);
        }


        @Override public abstract int hashCode();

        @Override public abstract boolean equals(@Nullable Object obj);
    }
}
